package com.canibunk.service;

import com.canibunk.ds.DoublyLinkedList;
import com.canibunk.model.CalendarDay;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Service to fetch, parse, and synchronize the official Amal Jyothi College of Engineering
 * Academic Calendar (from https://www.ajce.in/home/academic-calendar.html / Google Sheets).
 *
 * Provides accurate remaining working days, semester milestones, last instructional day,
 * condonation deadlines, and end-semester exam dates.
 */
public class AcademicCalendarService {

    public static final String AJCE_CALENDAR_PAGE_URL = "https://www.ajce.in/home/academic-calendar.html";
    public static final String LIVE_ODD_SEM_CSV_URL =
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vSivGfzCJf6BiIMume1nsVHfarXEsIZNvELHQwRL4gUfzLydLoRg3jQAZSmelcKkUZwT1lxIyLt0dq6/pub?output=csv&gid=75677686";
    public static final String LIVE_EVEN_SEM_CSV_URL =
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vSivGfzCJf6BiIMume1nsVHfarXEsIZNvELHQwRL4gUfzLydLoRg3jQAZSmelcKkUZwT1lxIyLt0dq6/pub?output=csv&gid=1415863841";

    public static final String DEFAULT_OFFLINE_CSV_PATH = "data/academic_calendar_s3.csv";

    public static class Milestone {
        private final String title;
        private final LocalDate date;
        private final String details;

        public Milestone(String title, LocalDate date, String details) {
            this.title = title;
            this.date = date;
            this.details = details;
        }

        public String getTitle() { return title; }
        public LocalDate getDate() { return date; }
        public String getDetails() { return details; }

        @Override
        public String toString() {
            return String.format("%s: %s (%s)", title, date, details);
        }
    }

    private boolean liveSynced = false;
    private String dataSource = "Not Loaded";
    private int totalInstructionalWorkingDays = 80;
    private LocalDate semesterStartDate = LocalDate.of(2026, 7, 1);
    private LocalDate lastInstructionalDay = LocalDate.of(2026, 11, 5);
    private LocalDate condonationStartDate = LocalDate.of(2026, 11, 6);
    private LocalDate condonationDeadlineNoFine = LocalDate.of(2026, 11, 9);
    private LocalDate condonationDeadlineWithFine = LocalDate.of(2026, 11, 11);
    private LocalDate firstExamDate = LocalDate.of(2026, 11, 13);
    private final List<Milestone> milestones = new ArrayList<>();

    public AcademicCalendarService() {}

    /**
     * Loads the academic calendar into the specified CalendarManager.
     * Tries live fetch first; if offline or fails, falls back to the bundled offline CSV.
     */
    public boolean loadCalendar(CalendarManager manager, boolean preferLive, boolean isS1) {
        if (preferLive) {
            try {
                boolean success = loadFromUrl(manager, isS1 ? LIVE_ODD_SEM_CSV_URL : LIVE_ODD_SEM_CSV_URL, isS1);
                if (success) {
                    this.liveSynced = true;
                    this.dataSource = "Live Google Sheet (AJCE Online Sync)";
                    return true;
                }
            } catch (Exception ignored) {
                // Seamlessly fallback to offline CSV
            }
        }

        File offlineFile = new File(DEFAULT_OFFLINE_CSV_PATH);
        if (offlineFile.exists()) {
            try {
                boolean success = loadFromFile(manager, offlineFile, isS1);
                if (success) {
                    this.liveSynced = false;
                    this.dataSource = "Bundled Offline Cache (" + offlineFile.getPath() + ")";
                    return true;
                }
            } catch (Exception e) {
                System.err.println("[AcademicCalendarService] Error loading offline CSV: " + e.getMessage());
            }
        }

        return false;
    }

    public boolean loadFromUrl(CalendarManager manager, String csvUrl, boolean isS1) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(csvUrl))
                .header("User-Agent", "Mozilla/5.0 (CanIBunk Academic Attendance Engine)")
                .GET()
                .build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() == 200) {
            try (InputStream in = response.body()) {
                return parseAndPopulate(manager, in, isS1);
            }
        }
        return false;
    }

    public boolean loadFromFile(CalendarManager manager, File file, boolean isS1) throws Exception {
        try (InputStream in = new FileInputStream(file)) {
            return parseAndPopulate(manager, in, isS1);
        }
    }

    /**
     * Parses the multi-month table structure from the AJCE calendar CSV.
     */
    public boolean parseAndPopulate(CalendarManager manager, InputStream in, boolean isS1) throws Exception {
        List<List<String>> records = parseCsv(in);
        if (records.isEmpty()) return false;

        List<String> header = records.get(0);
        // Header contains month columns: July 26, August 26, etc.
        // Identify month blocks: [DateCol, EventCol, DaysCol, S1Col]
        List<MonthBlock> monthBlocks = new ArrayList<>();
        for (int c = 2; c < header.size(); c += 4) {
            if (c + 2 < header.size()) {
                String monthName = header.get(c + 1).trim();
                YearMonth ym = parseYearMonth(monthName);
                if (ym != null) {
                    monthBlocks.add(new MonthBlock(c, c + 1, c + 2, (c + 3 < header.size()) ? c + 3 : -1, ym));
                }
            }
        }

        if (monthBlocks.isEmpty()) return false;

        Map<LocalDate, CalendarDay> dayMap = new TreeMap<>();
        int maxWorkingDay = 0;
        LocalDate detectedLastInstructional = null;

        // Records 1 to 37 are day entries
        for (int r = 1; r < records.size(); r++) {
            List<String> row = records.get(r);
            if (row.isEmpty()) continue;
            String dayLabel = (row.size() > 1) ? row.get(1).trim() : "";
            if (dayLabel.equalsIgnoreCase("Day") || dayLabel.startsWith("*") || dayLabel.startsWith("Abbreviation")) {
                break; // reached summary/footer section
            }

            for (MonthBlock mb : monthBlocks) {
                if (mb.dateCol < row.size()) {
                    String dateStr = row.get(mb.dateCol).trim();
                    if (!dateStr.isEmpty() && dateStr.matches("\\d+")) {
                        int dayOfMonth = Integer.parseInt(dateStr);
                        if (dayOfMonth >= 1 && dayOfMonth <= mb.yearMonth.lengthOfMonth()) {
                            LocalDate date = mb.yearMonth.atDay(dayOfMonth);
                            String event = (mb.eventCol < row.size()) ? row.get(mb.eventCol).replace("\n", " ").trim() : "";
                            int targetDayCol = isS1 && mb.s1Col != -1 ? mb.s1Col : mb.daysCol;
                            String wDayStr = (targetDayCol < row.size()) ? row.get(targetDayCol).trim() : "";

                            boolean isWorking = false;
                            int wDayNum = -1;
                            if (!wDayStr.isEmpty() && wDayStr.matches("\\d+")) {
                                wDayNum = Integer.parseInt(wDayStr);
                                isWorking = true;
                                if (wDayNum > maxWorkingDay && (event.contains("Instructional") || wDayNum <= 80)) {
                                    maxWorkingDay = wDayNum;
                                }
                            }

                            if (event.toLowerCase().contains("last instructional day") &&
                                    (isS1 ? event.toLowerCase().contains("s1") : event.toLowerCase().contains("s3") || event.toLowerCase().contains("s5") || event.toLowerCase().contains("s7") || !event.toLowerCase().contains("s1"))) {
                                detectedLastInstructional = date;
                            }

                            CalendarDay calDay = new CalendarDay(date, isWorking, event, wDayNum);
                            dayMap.put(date, calDay);
                        }
                    }
                }
            }
        }

        if (dayMap.isEmpty()) return false;

        // Clear existing manager and populate in strictly chronological order
        manager.getCalendarList().clear();
        for (CalendarDay cd : dayMap.values()) {
            manager.addDay(cd);
        }

        if (maxWorkingDay > 0) {
            this.totalInstructionalWorkingDays = maxWorkingDay;
        }
        if (detectedLastInstructional != null) {
            this.lastInstructionalDay = detectedLastInstructional;
        }

        extractMilestones(dayMap, isS1);
        return true;
    }

    private void extractMilestones(Map<LocalDate, CalendarDay> dayMap, boolean isS1) {
        milestones.clear();
        for (CalendarDay day : dayMap.values()) {
            String desc = day.getDescription();
            if (desc.isEmpty()) continue;
            String lower = desc.toLowerCase();

            if (lower.contains("commencement") && (isS1 ? lower.contains("s1") : lower.contains("s3"))) {
                this.semesterStartDate = day.getDate();
                milestones.add(new Milestone("Semester Commencement", day.getDate(), desc));
            } else if (lower.contains("cie 1") && !milestones.stream().anyMatch(m -> m.getTitle().equals("CIE 1 (Series Exam 1)"))) {
                milestones.add(new Milestone("CIE 1 (Series Exam 1)", day.getDate(), desc));
            } else if (lower.contains("cie 2") && !milestones.stream().anyMatch(m -> m.getTitle().equals("CIE 2 (Series Exam 2)"))) {
                milestones.add(new Milestone("CIE 2 (Series Exam 2)", day.getDate(), desc));
            } else if (lower.contains("last instructional day") && (isS1 ? lower.contains("s1") : !lower.contains("(s1)"))) {
                this.lastInstructionalDay = day.getDate();
                milestones.add(new Milestone("Last Instructional Day (Classes End)", day.getDate(), desc));
            } else if (lower.contains("condonation") && lower.contains("without fine")) {
                this.condonationDeadlineNoFine = day.getDate();
                milestones.add(new Milestone("Condonation Deadline (No Fine)", day.getDate(), desc));
            } else if (lower.contains("condonation") && lower.contains("with") && lower.contains("fine")) {
                this.condonationDeadlineWithFine = day.getDate();
                milestones.add(new Milestone("Condonation Deadline (With Fine)", day.getDate(), desc));
            } else if (lower.contains("apply for condonation") && !milestones.stream().anyMatch(m -> m.getTitle().startsWith("Condonation Application Starts"))) {
                this.condonationStartDate = day.getDate();
                milestones.add(new Milestone("Condonation Application Starts", day.getDate(), desc));
            } else if ((lower.contains("ese 1") || lower.contains("s3/s5/s7 ese 1")) && !milestones.stream().anyMatch(m -> m.getTitle().equals("End Semester Exam 1 (ESE 1)"))) {
                this.firstExamDate = day.getDate();
                milestones.add(new Milestone("End Semester Exam 1 (ESE 1)", day.getDate(), desc));
            }
        }
    }

    /**
     * Traverses forward from the given reference date up to the Last Instructional Day,
     * counting exactly how many instructional working days remain.
     */
    public int countWorkingDaysRemaining(CalendarManager manager, LocalDate fromDate) {
        if (fromDate.isAfter(lastInstructionalDay)) {
            return 0;
        }
        int count = 0;
        for (CalendarDay day : manager.getCalendarList()) {
            if (!day.getDate().isBefore(fromDate) && !day.getDate().isAfter(lastInstructionalDay)) {
                if (day.isWorkingDay()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Counts how many instructional working days have passed from semester start up to the reference date.
     */
    public int countWorkingDaysElapsed(CalendarManager manager, LocalDate asOfDate) {
        int count = 0;
        for (CalendarDay day : manager.getCalendarList()) {
            if (!day.getDate().isBefore(semesterStartDate) && !day.getDate().isAfter(asOfDate)) {
                if (day.isWorkingDay()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns a list of all remaining instructional working days between fromDate and lastInstructionalDay.
     */
    public List<CalendarDay> getRemainingInstructionalDays(CalendarManager manager, LocalDate fromDate) {
        List<CalendarDay> list = new ArrayList<>();
        for (CalendarDay day : manager.getCalendarList()) {
            if (!day.getDate().isBefore(fromDate) && !day.getDate().isAfter(lastInstructionalDay)) {
                if (day.isWorkingDay()) {
                    list.add(day);
                }
            }
        }
        return list;
    }

    private static YearMonth parseYearMonth(String header) {
        // e.g. "July 26" -> 2026-07, "August 26" -> 2026-08, "January 27" -> 2027-01
        String[] parts = header.trim().split("[\\s_-]+");
        if (parts.length >= 2) {
            String monthPart = parts[0].toLowerCase();
            String yearPart = parts[1];
            int year = 2000;
            try {
                int y = Integer.parseInt(yearPart.replaceAll("\\D", ""));
                year = (y < 100) ? 2000 + y : y;
            } catch (Exception e) {
                return null;
            }

            int month = 0;
            if (monthPart.startsWith("jan")) month = 1;
            else if (monthPart.startsWith("feb")) month = 2;
            else if (monthPart.startsWith("mar")) month = 3;
            else if (monthPart.startsWith("apr")) month = 4;
            else if (monthPart.startsWith("may")) month = 5;
            else if (monthPart.startsWith("jun")) month = 6;
            else if (monthPart.startsWith("jul")) month = 7;
            else if (monthPart.startsWith("aug")) month = 8;
            else if (monthPart.startsWith("sep")) month = 9;
            else if (monthPart.startsWith("oct")) month = 10;
            else if (monthPart.startsWith("nov")) month = 11;
            else if (monthPart.startsWith("dec")) month = 12;

            if (month > 0) {
                return YearMonth.of(year, month);
            }
        }
        return null;
    }

    public static List<List<String>> parseCsv(InputStream in) throws Exception {
        List<List<String>> records = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        List<String> currentRecord = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        int ch;

        while ((ch = reader.read()) != -1) {
            char c = (char) ch;
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                currentRecord.add(currentField.toString().trim());
                currentField.setLength(0);
            } else if ((c == '\r' || c == '\n') && !inQuotes) {
                if (c == '\r') {
                    reader.mark(1);
                    int next = reader.read();
                    if (next != '\n') {
                        reader.reset();
                    }
                }
                currentRecord.add(currentField.toString().trim());
                currentField.setLength(0);
                if (!currentRecord.isEmpty() && !(currentRecord.size() == 1 && currentRecord.get(0).isEmpty())) {
                    records.add(new ArrayList<>(currentRecord));
                }
                currentRecord.clear();
            } else {
                currentField.append(c);
            }
        }
        if (currentField.length() > 0 || !currentRecord.isEmpty()) {
            currentRecord.add(currentField.toString().trim());
            records.add(currentRecord);
        }
        return records;
    }

    private static class MonthBlock {
        final int dateCol;
        final int eventCol;
        final int daysCol;
        final int s1Col;
        final YearMonth yearMonth;

        MonthBlock(int dateCol, int eventCol, int daysCol, int s1Col, YearMonth yearMonth) {
            this.dateCol = dateCol;
            this.eventCol = eventCol;
            this.daysCol = daysCol;
            this.s1Col = s1Col;
            this.yearMonth = yearMonth;
        }
    }

    public boolean isLiveSynced() { return liveSynced; }
    public String getDataSource() { return dataSource; }
    public int getTotalInstructionalWorkingDays() { return totalInstructionalWorkingDays; }
    public LocalDate getSemesterStartDate() { return semesterStartDate; }
    public LocalDate getLastInstructionalDay() { return lastInstructionalDay; }
    public LocalDate getCondonationStartDate() { return condonationStartDate; }
    public LocalDate getCondonationDeadlineNoFine() { return condonationDeadlineNoFine; }
    public LocalDate getCondonationDeadlineWithFine() { return condonationDeadlineWithFine; }
    public LocalDate getFirstExamDate() { return firstExamDate; }
    public List<Milestone> getMilestones() { return milestones; }
}

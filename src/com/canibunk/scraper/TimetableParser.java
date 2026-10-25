package com.canibunk.scraper;

import com.canibunk.ds.CustomHashMap;
import com.canibunk.model.ClassSession;
import com.canibunk.model.Subject;
import com.canibunk.service.ScheduleManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for College Timetable Schedule HTML pages (e.g., aesajce.in/students/classworkSchedule.php).
 * Parses the schedule grid (Days x Periods) and populates ScheduleManager's CustomQueue (FIFO).
 */
public class TimetableParser {

    private static final String[] DEFAULT_SLOTS = {
            "09:00 - 10:00", // Period 1
            "10:00 - 11:00", // Period 2
            "11:15 - 12:15", // Period 3
            "01:15 - 02:15", // Period 4
            "02:15 - 03:15", // Period 5
            "03:15 - 04:15", // Period 6
            "04:15 - 05:00"  // Period 7
    };

    private static final Pattern CODE_PATTERN = Pattern.compile("\\b((?:\\d{2})?[A-Z]{2,4}\\s*\\d{3}[A-Z]?)\\b");
    private static final Pattern ROOM_PATTERN = Pattern.compile("(?i)\\b(LH[-\\s]*\\d+|CR[-\\s]*\\d+|CC[-\\s]*LAB\\s*\\d*|LAB[-\\s]*\\d*|HALL[-\\s]*\\d*|ROOM[-\\s]*\\d+)\\b");
    private static final Pattern PAREN_PATTERN = Pattern.compile("\\(([^)]+)\\)");

    /**
     * Container holding the parsed weekly schedule.
     */
    public static class TimetableResult {
        private final Map<String, List<ClassSession>> scheduleByDay = new LinkedHashMap<>();

        public void addSession(String day, ClassSession session) {
            String normalizedDay = day.trim().toUpperCase();
            scheduleByDay.computeIfAbsent(normalizedDay, k -> new ArrayList<>()).add(session);
        }

        public Map<String, List<ClassSession>> getScheduleByDay() {
            return scheduleByDay;
        }

        public List<ClassSession> getSessionsForDay(String day) {
            if (day == null) return Collections.emptyList();
            String key = day.trim().toUpperCase();
            return scheduleByDay.getOrDefault(key, Collections.emptyList());
        }

        public List<ClassSession> getSessionsForDate(LocalDate date) {
            if (date == null) return Collections.emptyList();
            return getSessionsForDay(date.getDayOfWeek().name());
        }

        public int getTotalSessions() {
            int count = 0;
            for (List<ClassSession> list : scheduleByDay.values()) {
                count += list.size();
            }
            return count;
        }

        public boolean isEmpty() {
            return getTotalSessions() == 0;
        }

        public String formatWeeklyTable() {
            StringBuilder sb = new StringBuilder();
            sb.append("════════════════════════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                                    📅 WEEKLY CLASS TIMETABLE GRID                                  \n");
            sb.append("════════════════════════════════════════════════════════════════════════════════════════════════════\n");

            if (isEmpty()) {
                sb.append("  (No timetable records found)\n");
                sb.append("════════════════════════════════════════════════════════════════════════════════════════════════════\n");
                return sb.toString();
            }

            for (Map.Entry<String, List<ClassSession>> entry : scheduleByDay.entrySet()) {
                String day = entry.getKey();
                List<ClassSession> sessions = entry.getValue();
                sb.append(String.format("\n🔹 %-10s (%d periods):\n", day, sessions.size()));
                for (ClassSession s : sessions) {
                    sb.append(String.format("   Period %d [%s] ➔ [%-8s] %-32s @ Room: %s\n",
                            s.getPeriodNumber(),
                            s.getTimeSlot(),
                            s.getSubjectCode(),
                            s.getSubjectName().length() > 32 ? s.getSubjectName().substring(0, 29) + "..." : s.getSubjectName(),
                            s.getClassroom()));
                }
            }
            sb.append("\n════════════════════════════════════════════════════════════════════════════════════════════════════\n");
            return sb.toString();
        }
    }

    /**
     * Parses timetable HTML into a TimetableResult.
     *
     * @param htmlContent full HTML source or table HTML snippet
     * @param registry optional subject registry to resolve course names
     * @return TimetableResult containing day-wise class sessions
     */
    public static TimetableResult parseTimetable(String htmlContent, CustomHashMap<String, Subject> registry) {
        TimetableResult result = new TimetableResult();
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return result;
        }

        // Match table rows <tr>...</tr>
        Pattern rowPattern = Pattern.compile("(?is)<tr[^>]*>(.*?)</tr>");
        Matcher rowMatcher = rowPattern.matcher(htmlContent);

        List<String> periodTimeSlots = new ArrayList<>();
        boolean foundGridHeader = false;

        while (rowMatcher.find()) {
            String rowHtml = rowMatcher.group(1);
            List<String> cells = extractCells(rowHtml);

            if (cells.isEmpty()) continue;

            // Check if this row is the column header for periods (e.g. Day, 1, 2, 3... or times)
            if (!foundGridHeader && isTimetableHeaderRow(cells)) {
                foundGridHeader = true;
                // Parse time slots / period labels from columns (skip col 0 which is usually "Day")
                for (int c = 1; c < cells.size(); c++) {
                    String slot = cleanTimeSlot(cells.get(c), c);
                    periodTimeSlots.add(slot);
                }
                continue;
            }

            // Check if first cell is a day of week (Monday, Tuesday, etc.)
            String day = identifyDay(cells.get(0));
            if (day != null) {
                int periodNum = 1;
                for (int c = 1; c < cells.size(); c++) {
                    String cellContent = cells.get(c).trim();
                    if (isFreeOrEmptyPeriod(cellContent)) {
                        periodNum++;
                        continue;
                    }

                    String timeSlot = (c - 1 < periodTimeSlots.size())
                            ? periodTimeSlots.get(c - 1)
                            : getDefaultSlot(periodNum);

                    ClassSession session = parseCellToSession(periodNum, timeSlot, cellContent, registry);
                    if (session != null) {
                        result.addSession(day, session);
                    }
                    periodNum++;
                }
            }
        }

        return result;
    }

    /**
     * Overload parsing without registry.
     */
    public static TimetableResult parseTimetable(String htmlContent) {
        return parseTimetable(htmlContent, null);
    }

    /**
     * Populates ScheduleManager with sessions for today's date.
     * If today is a weekend or has no classes, loads Monday's schedule as default preview.
     *
     * @return number of sessions enqueued into ScheduleManager
     */
    public static int populateTodaySchedule(TimetableResult timetable, ScheduleManager scheduleManager, CustomHashMap<String, Subject> registry) {
        if (timetable == null || scheduleManager == null) return 0;

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String dayKey = today.name();
        List<ClassSession> sessions = timetable.getSessionsForDay(dayKey);

        // If today is a weekend or empty, fallback to Monday for demo/preview
        if (sessions.isEmpty()) {
            sessions = timetable.getSessionsForDay("MONDAY");
        }

        if (!sessions.isEmpty()) {
            scheduleManager.clear();
            for (ClassSession s : sessions) {
                // If registry has more up-to-date subject name, enhance it
                String subName = s.getSubjectName();
                if (registry != null && registry.containsKey(s.getSubjectCode())) {
                    subName = registry.get(s.getSubjectCode()).getSubjectName();
                }
                ClassSession enhanced = new ClassSession(
                        s.getPeriodNumber(),
                        s.getTimeSlot(),
                        s.getSubjectCode(),
                        subName,
                        s.getClassroom()
                );
                scheduleManager.addSession(enhanced);
            }
            return sessions.size();
        }

        return 0;
    }

    private static ClassSession parseCellToSession(int periodNum, String timeSlot, String cellContent, CustomHashMap<String, Subject> registry) {
        String code = null;
        String name = null;
        String room = "LH-101";

        // 1. Extract subject code
        Matcher codeMatcher = CODE_PATTERN.matcher(cellContent);
        if (codeMatcher.find()) {
            code = codeMatcher.group(1).replaceAll("\\s+", "");
        } else {
            // Fallback for uppercase alphanumeric token (4-10 chars)
            String[] tokens = cellContent.split("[\\s,;\\n\\r]+");
            for (String t : tokens) {
                String clean = t.replaceAll("[^A-Za-z0-9]", "");
                if (clean.matches("^[A-Z0-9]{4,10}$") && !clean.matches("^\\d+$")) {
                    code = clean;
                    break;
                }
            }
        }

        if (code == null) {
            return null; // No valid course code in this cell
        }

        // 2. Extract Room / Classroom
        Matcher roomMatcher = ROOM_PATTERN.matcher(cellContent);
        if (roomMatcher.find()) {
            room = roomMatcher.group(1).trim();
        } else {
            Matcher parenMatcher = PAREN_PATTERN.matcher(cellContent);
            if (parenMatcher.find()) {
                String candidate = parenMatcher.group(1).trim();
                if (candidate.length() <= 12 && !candidate.equalsIgnoreCase(code)) {
                    room = candidate;
                }
            }
        }

        // 3. Resolve Subject Name
        if (registry != null && registry.containsKey(code)) {
            name = registry.get(code).getSubjectName();
        } else {
            // Clean out code, room, and parentheses to isolate name
            String remaining = cellContent.replace(code, "")
                    .replaceAll("\\([^)]*\\)", "")
                    .replaceAll("(?i)\\b(LH|CR|LAB|ROOM)[-\\s]*\\d+\\b", "")
                    .replaceAll("[-/|]+", " ")
                    .trim();
            if (remaining.length() > 3) {
                name = remaining;
            } else {
                name = code; // Fallback to code if name not in cell
            }
        }

        return new ClassSession(periodNum, timeSlot, code, name, room);
    }

    private static String identifyDay(String text) {
        if (text == null) return null;
        String t = text.trim().toUpperCase();
        if (t.contains("MON")) return "MONDAY";
        if (t.contains("TUE")) return "TUESDAY";
        if (t.contains("WED")) return "WEDNESDAY";
        if (t.contains("THU")) return "THURSDAY";
        if (t.contains("FRI")) return "FRIDAY";
        if (t.contains("SAT")) return "SATURDAY";
        if (t.contains("SUN")) return "SUNDAY";
        return null;
    }

    private static boolean isTimetableHeaderRow(List<String> cells) {
        String combined = String.join(" ", cells).toLowerCase();
        boolean hasDayCol = combined.contains("day") || combined.contains("days") || combined.contains("day/hour");
        boolean hasPeriodCol = combined.contains("1") || combined.contains("period") || combined.contains("hour")
                || combined.contains("09:") || combined.contains("10:");
        return hasDayCol || (cells.size() >= 5 && hasPeriodCol);
    }

    private static boolean isFreeOrEmptyPeriod(String cell) {
        if (cell == null || cell.trim().isEmpty()) return true;
        String lower = cell.toLowerCase().trim();
        return lower.equals("-") || lower.equals("--") || lower.equals("nil")
                || lower.equals("free") || lower.equals("lunch") || lower.equals("break")
                || lower.equals("interval");
    }

    private static String cleanTimeSlot(String cellText, int periodIdx) {
        // Check if cell has time format like 09:00 - 10:00 or 9:00-10:00
        Matcher m = Pattern.compile("(\\d{1,2}:\\d{2}\\s*(?:-|to)\\s*\\d{1,2}:\\d{2})").matcher(cellText);
        if (m.find()) {
            return m.group(1);
        }
        return getDefaultSlot(periodIdx);
    }

    private static String getDefaultSlot(int periodNum) {
        if (periodNum >= 1 && periodNum <= DEFAULT_SLOTS.length) {
            return DEFAULT_SLOTS[periodNum - 1];
        }
        return String.format("Period %d", periodNum);
    }

    private static List<String> extractCells(String rowHtml) {
        List<String> cells = new ArrayList<>();
        Pattern cellPattern = Pattern.compile("(?is)<(?:td|th)[^>]*>(.*?)</(?:td|th)>");
        Matcher matcher = cellPattern.matcher(rowHtml);
        while (matcher.find()) {
            String cleanText = cleanHtmlTags(matcher.group(1)).trim();
            cells.add(cleanText);
        }
        return cells;
    }

    private static String cleanHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

package com.canibunk;

import com.canibunk.ds.CustomHashMap;
import com.canibunk.model.*;
import com.canibunk.service.*;
import com.canibunk.scraper.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * CanIBunk.com - Phase 1 Core CLI Application
 * An intelligent college attendance monitor and bunkability predictor powered by
 * custom Data Structures (CustomHashMap, CustomStack, CustomQueue, DoublyLinkedList, AttendanceBST).
 */
public class Main {

    private static final CustomHashMap<String, Subject> subjectRegistry = new CustomHashMap<>();
    private static final DangerZoneService dangerZoneService = new DangerZoneService();
    private static final ScheduleManager scheduleManager = new ScheduleManager();
    private static final WhatIfSimulator whatIfSimulator = new WhatIfSimulator();
    private static final CalendarManager calendarManager = new CalendarManager();
    private static final AcademicCalendarService calendarService = new AcademicCalendarService();

    public static void main(String[] args) {
        initSampleData();

        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            runAutomatedWalkthrough();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        printBanner();

        // 1. Initial Prompt: Ask user to log in to AES portal
        promptAesLoginAtStartup(scanner);

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("👉 Enter choice (0-6): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    viewTodaysSchedule(scanner);
                    break;
                case "2":
                    displayAttendanceDashboard();
                    break;
                case "3":
                    displayAcademicCalendarDLL(scanner);
                    break;
                case "4":
                    checkSubjectBunkability(scanner);
                    break;
                case "5":
                    launchWhatIfSimulator(scanner);
                    break;
                case "6":
                    runLiveSeleniumScraper(scanner);
                    break;
                case "0":
                    System.out.println("\n👋 Thank you for using CanIBunk.com! Attend your classes responsibly.");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please choose between 0 and 6.");
            }
        }
    }

    private static void promptAesLoginAtStartup(Scanner scanner) {
        System.out.println("\n🔑 COLLEGE PORTAL AUTHENTICATION (aesajce.in)");
        System.out.println("CanIBunk can fetch your live attendance and class schedule directly from your student portal.");
        System.out.print("👉 Would you like to log in to AES portal now? (y/N): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("y") || choice.equals("yes")) {
            runLiveSeleniumScraper(scanner);
        } else {
            // Check if debug files exist or fallback to realistic default data
            java.nio.file.Path attPath = java.nio.file.Path.of("scraped_attendance_debug.html");
            if (java.nio.file.Files.exists(attPath)) {
                System.out.println("ℹ️ Offline mode: Found cached portal snapshot on disk ('scraped_attendance_debug.html'). Loaded cached data.");
                try {
                    String attendanceHtml = java.nio.file.Files.readString(attPath);
                    List<Subject> parsed = AttendanceTableParser.parseHtmlTable(attendanceHtml);
                    if (!parsed.isEmpty()) {
                        AttendanceTableParser.populateRegistry(parsed, subjectRegistry, true);
                        dangerZoneService.rebuildTree(subjectRegistry.values());
                    }
                } catch (Exception ignored) {}
            } else {
                System.out.println("ℹ️ Proceeding with offline sample attendance and timetable data.");
            }
        }
    }

    private static void printBanner() {
        System.out.println("\n" +
                "========================================================================\n" +
                "  🎓 CanIBunk.com - Intelligent Attendance Monitor & Bunk Predictor    \n" +
                "  [Semester 3: Java & Data Structures Combined Academic Project]        \n" +
                "  Standard Rule: 75.0% Strict Attendance Threshold                      \n" +
                "========================================================================");
    }

    private static void printMenu() {
        System.out.println("\n─────────────── MAIN MENU ───────────────");
        System.out.println(" 1. ⏰ View Today's Schedule (Check Day & Upcoming Classes)");
        System.out.println(" 2. 📊 View My Attendance Status (All Subjects & %)");
        System.out.println(" 3. 📅 Access Academic Calendar (Working Days Left & Milestones)");
        System.out.println(" 4. 🎯 Check Bunkability for a Subject (Choose Subject & % Drop)");
        System.out.println(" 5. 🔮 'What-If' Attendance Simulator (CustomStack Undo/Redo)");
        System.out.println(" 6. 🌐 Sync / Log in to College AES Portal");
        System.out.println(" 0. 🚪 Exit");
        System.out.println("─────────────────────────────────────────");
    }

    /**
     * Initializes realistic hardcoded college data (Semester 3 CSE / KTU / AJCE format).
     */
    private static void initSampleData() {
        subjectRegistry.put("CS201", new Subject("CS201", "Discrete Mathematical Structures", 22, 25, 4)); // 88.0% (Safe)
        subjectRegistry.put("CS203", new Subject("CS203", "Data Structures & Algorithms", 19, 24, 4));    // 79.17% (Edge)
        subjectRegistry.put("CS205", new Subject("CS205", "Object Oriented Programming (Java)", 24, 26, 4)); // 92.31% (Safe)
        subjectRegistry.put("CS207", new Subject("CS207", "Digital Electronics & Logic Design", 14, 20, 3)); // 70.0% (Danger)
        subjectRegistry.put("EST200", new Subject("EST200", "Design & Engineering", 9, 14, 2));            // 64.29% (Critical)
        subjectRegistry.put("CSL201", new Subject("CSL201", "Data Structures Lab", 18, 18, 2));             // 100.0% (Rock Solid)

        dangerZoneService.rebuildTree(subjectRegistry.values());

        // Initialize weekly schedule
        TimetableParser.TimetableResult sampleTt = getSampleTimetableResult();
        scheduleManager.setWeeklySchedule(sampleTt.getScheduleByDay());

        // Daily Timetable Queue for Today
        scheduleManager.addSession(new ClassSession(1, "09:00 - 10:00", "CS203", "Data Structures & Algorithms", "LH-101"));
        scheduleManager.addSession(new ClassSession(2, "10:00 - 11:00", "CS207", "Digital Electronics", "LH-101"));
        scheduleManager.addSession(new ClassSession(3, "11:15 - 12:15", "CS205", "Java Programming", "CC-Lab 2"));
        scheduleManager.addSession(new ClassSession(4, "01:15 - 02:15", "CS201", "Discrete Mathematics", "LH-101"));
        scheduleManager.addSession(new ClassSession(5, "02:15 - 03:15", "EST200", "Design & Engineering", "LH-104"));

        // Load Official AJCE Academic Calendar (Live fetch from Google Sheet or bundled offline CSV)
        boolean calendarLoaded = calendarService.loadCalendar(calendarManager, true, false);
        if (!calendarLoaded || calendarManager.getTotalDays() == 0) {
            LocalDate base = LocalDate.of(2026, 10, 19);
            calendarManager.addDay(new CalendarDay(base, true, "Regular Working Day", 41));
            calendarManager.addDay(new CalendarDay(base.plusDays(1), true, "Regular Working Day - Today", 42));
            calendarManager.addDay(new CalendarDay(base.plusDays(2), true, "Regular Working Day", 43));
            calendarManager.addDay(new CalendarDay(base.plusDays(3), true, "Regular Working Day", 44));
            calendarManager.addDay(new CalendarDay(base.plusDays(4), true, "Regular Working Day", 45));
            calendarManager.addDay(new CalendarDay(base.plusDays(5), false, "Saturday - Weekend", 46));
            calendarManager.addDay(new CalendarDay(base.plusDays(6), false, "Sunday - Weekend", 47));
            calendarManager.addDay(new CalendarDay(base.plusDays(7), false, "Diwali Festival Holiday", 48));
            calendarManager.addDay(new CalendarDay(base.plusDays(8), true, "Regular Working Day", 49));
            calendarManager.addDay(new CalendarDay(base.plusDays(9), true, "Series Exam 2 (Internal)", 50));
            calendarManager.addDay(new CalendarDay(base.plusDays(10), true, "Series Exam 2 (Internal)", 51));
            calendarManager.addDay(new CalendarDay(base.plusDays(11), true, "Series Exam 2 (Internal)", 52));
            calendarManager.addDay(new CalendarDay(base.plusDays(12), false, "Weekend", 53));
            calendarManager.addDay(new CalendarDay(base.plusDays(13), false, "Weekend", 54));
            calendarManager.addDay(new CalendarDay(base.plusDays(14), true, "End Semester Revision Starts", 55));
        }
    }

    /**
     * Feature 1: Full Attendance Matrix (CustomHashMap O(1) lookup).
     */
    private static void displayAttendanceDashboard() {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                         📊 ATTENDANCE MONITOR DASHBOARD (O(1) Map)                 ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-8s %-32s %-12s %-10s %-12s %-10s %-15s\n",
                "CODE", "SUBJECT NAME", "ATTENDED", "PERCENT", "BUNKS LEFT", "NEEDED", "NEXT HOUR");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        for (Subject s : subjectRegistry.values()) {
            String bunkStatus = s.isBunkableNextHour() ? "\u001B[32m✔ YES\u001B[0m" : "\u001B[31m✖ NO\u001B[0m";
            String pctFormatted = s.getStatus().format(String.format("%6.2f%%", s.getAttendancePercentage()));

            System.out.printf("%-8s %-32s %-12s %-19s %-12d %-10d %-15s\n",
                    s.getSubjectCode(),
                    truncate(s.getSubjectName(), 30),
                    s.getAttendedClasses() + "/" + s.getTotalClasses(),
                    pctFormatted,
                    s.getSafeBunks(),
                    s.getClassesNeededTo75(),
                    bunkStatus
            );
        }
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Feature 2: Danger Zone Sorting via AttendanceBST (In-Order Traversal).
     */
    private static void displayDangerZoneBST() {
        dangerZoneService.rebuildTree(subjectRegistry.values());
        List<Subject> sorted = dangerZoneService.getSortedSubjects();

        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("               ⚠️  DANGER ZONE RANKING (AttendanceBST In-Order Traversal)             ");
        System.out.println("       [Subjects ordered strictly from Most In Danger (Lowest %) to Safest (Highest %)]  ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");

        int rank = 1;
        for (Subject s : sorted) {
            String badge;
            if (s.getAttendancePercentage() < 75.0) {
                badge = "\u001B[31m[CRITICAL DANGER]\u001B[0m Needs to attend " + s.getClassesNeededTo75() + " classes straight!";
            } else if (s.getAttendancePercentage() < 80.0 || s.getSafeBunks() == 0) {
                badge = "\u001B[33m[ON THE EDGE]\u001B[0m Above 75% but cannot bunk next hour!";
            } else {
                badge = "\u001B[32m[SAFE ZONE]\u001B[0m Can bunk " + s.getSafeBunks() + " more classes.";
            }

            System.out.printf("Rank #%d: [%-6s] %-30s | %5.2f%% | %s\n",
                    rank++,
                    s.getSubjectCode(),
                    truncate(s.getSubjectName(), 28),
                    s.getAttendancePercentage(),
                    badge
            );
        }

        Subject critical = dangerZoneService.getMostCriticalSubject();
        Subject safest = dangerZoneService.getSafestSubject();

        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("🚨 Most Critical Subject in Tree: [%s] %.2f%%\n",
                critical != null ? critical.getSubjectCode() + " - " + critical.getSubjectName() : "None",
                critical != null ? critical.getAttendancePercentage() : 0.0);
        System.out.printf("🛡️  Safest Subject in Tree       : [%s] %.2f%%\n",
                safest != null ? safest.getSubjectCode() + " - " + safest.getSubjectName() : "None",
                safest != null ? safest.getAttendancePercentage() : 0.0);

        System.out.println("\n--- BST Structure (Visual Hierarchy) ---");
        dangerZoneService.getBST().printTree();
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Menu Option 1: View Today's Schedule.
     * Checks the day of week:
     * - If Saturday or Sunday: displays a friendly message to relax / take rest / you are free!
     * - Otherwise: displays today's schedule and queue analysis.
     */
    private static void viewTodaysSchedule(Scanner scanner) {
        java.time.DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("                       ⏰ TODAY'S CLASS SCHEDULE (%s)                                \n", dayOfWeek.name());
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");

        if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            System.out.println("\n  🏖️  Woohoo! Today is " + dayOfWeek.name() + "!");
            System.out.println("  🎉 Take rest! You are completely free today! No college classes scheduled.");
            System.out.println("  ☕ Enjoy your weekend and recharge for the upcoming week!\n");
            System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");

            if (scanner != null) {
                System.out.println("Options: [W] View Full Weekly Timetable Grid | [Enter] Return to Menu");
                System.out.print("👉 Choice: ");
                String opt = scanner.nextLine().trim();
                if (opt.equalsIgnoreCase("w")) {
                    displayWeeklyTimetableGrid(scanner);
                }
            }
            return;
        }

        // Weekday: Load today's schedule into the FIFO queue if not already active
        String dayName = dayOfWeek.name();
        scheduleManager.loadDay(dayName);

        // Display the complete daily schedule with instant skip impact analysis table
        System.out.println(scheduleManager.formatDayScheduleAnalysis(dayName, subjectRegistry));

        System.out.printf("Active Day Queue: %s | Periods waiting in FIFO Queue: %d\n",
                scheduleManager.getActiveDayName(), scheduleManager.getRemainingClassesCount());
        ClassSession next = scheduleManager.peekUpcomingClass();
        if (next != null) {
            System.out.println("Next upcoming period in Queue: " + next);
        }

        if (scanner != null) {
            System.out.println("\nOptions:");
            System.out.println("  [Enter] Advance 1 period (calls dequeue() and evaluates live bunkability)");
            System.out.println("  [L]     Load a different day (e.g. MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)");
            System.out.println("  [W]     View full weekly timetable grid");
            System.out.println("  [B]     Back to main menu");
            System.out.print("👉 Choose option: ");
            String subChoice = scanner.nextLine().trim();

            if (subChoice.equalsIgnoreCase("b")) {
                return;
            } else if (subChoice.equalsIgnoreCase("w")) {
                displayWeeklyTimetableGrid(scanner);
                return;
            } else if (subChoice.equalsIgnoreCase("l")) {
                System.out.print("👉 Enter day name (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY): ");
                String dayInput = scanner.nextLine().trim().toUpperCase();
                if (scheduleManager.loadDay(dayInput)) {
                    System.out.printf("\u001B[32m✔ Loaded %d period(s) for %s into the Queue!\u001B[0m\n",
                            scheduleManager.getRemainingClassesCount(), dayInput);
                    System.out.println(scheduleManager.formatDayScheduleAnalysis(dayInput, subjectRegistry));
                } else {
                    System.out.println("⚠️ No schedule found for " + dayInput + ". Keeping current queue.");
                }
                return;
            }

            System.out.println("\nAdvancing 1 hour in Schedule Queue (calling dequeue())...");
            String evaluation = scheduleManager.evaluateAndAdvance(subjectRegistry);
            System.out.println(evaluation);
        }
    }

    /**
     * Menu Option 4: Check for Bunkability.
     * Shows all subjects, allows choosing a subject (or ALL), and shows:
     * - Current attendance %
     * - New attendance % if bunked 1 hour
     * - Percentage drop (-X.XX%)
     * - Safe bunks left
     * - Classes needed if below 75%
     * - Clear Bunkability verdict
     */
    private static void checkSubjectBunkability(Scanner scanner) {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                  🎯 BUNKABILITY EVALUATION & 1-HOUR DROP CALCULATOR                        ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("Available Subjects: " + subjectRegistry.keySet());
        System.out.println("Enter a Subject Code to inspect (e.g. CS203), or type 'ALL' to inspect all subjects:");
        System.out.print("👉 Subject Code: ");
        String codeInput = (scanner != null) ? scanner.nextLine().trim().toUpperCase() : "ALL";

        if (codeInput.isEmpty() || codeInput.equalsIgnoreCase("ALL")) {
            displayAllSubjectsBunkabilityDrop();
            return;
        }

        Subject s = subjectRegistry.get(codeInput);
        if (s == null) {
            System.out.println("❌ Subject code '" + codeInput + "' not found in registry!");
            return;
        }

        displaySingleSubjectBunkability(s);
    }

    private static void displaySingleSubjectBunkability(Subject s) {
        int attended = s.getAttendedClasses();
        int conducted = s.getTotalClasses();
        double currentPct = s.getAttendancePercentage();
        double bunk1Pct = s.getProjectedPercentageIfBunk();
        double attend1Pct = s.getProjectedPercentageIfAttend();
        double drop = currentPct - bunk1Pct;
        int safeBunks = s.getSafeBunks();
        int needed = s.getClassesNeededTo75();
        boolean safeToBunkNext = s.isBunkableNextHour();

        System.out.println("\n────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("  📘 Subject Code    : \u001B[33m%s\u001B[0m (%s)\n", s.getSubjectCode(), s.getSubjectName());
        System.out.printf("  📊 Classes Attended: %d / %d conducted\n", attended, conducted);
        System.out.printf("  📈 Current Status  : \u001B[1m%.2f%%\u001B[0m  [%s]\n",
                currentPct, s.getStatus().format(s.getStatus().name()));
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.println("  💥 1-HOUR BUNK IMPACT:");
        System.out.printf("     • If you bunk 1 hour : \u001B[31m%.2f%%\u001B[0m (attended %d / %d)\n", bunk1Pct, attended, conducted + 1);
        System.out.printf("     • Percentage Drop    : \u001B[31m-%.2f%%\u001B[0m drop in attendance\n", drop);
        System.out.printf("     • If you attend 1 hr : \u001B[32m%.2f%%\u001B[0m (attended %d / %d, +%.2f%% gain)\n",
                attend1Pct, attended + 1, conducted + 1, (attend1Pct - currentPct));
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.println("  🛡️  BUNK SAFETY & RECOVERY:");
        System.out.printf("     • Safe Bunks Left    : \u001B[32m%d class(es)\u001B[0m you can bunk before falling below 75.0%%\n", safeBunks);
        if (currentPct < 75.0) {
            System.out.printf("     • Recovery Needed    : \u001B[31mMust attend %d class(es) straight\u001B[0m without bunking to reach 75.0%%!\n", needed);
        }
        System.out.print("  🚦 VERDICT FOR NEXT CLASS : ");
        if (safeToBunkNext) {
            System.out.println("\u001B[32m✔ YES, SAFE TO BUNK! (Attendance will stay at " + String.format("%.2f%%", bunk1Pct) + " >= 75%)\u001B[0m");
        } else {
            System.out.println("\u001B[31m✖ DO NOT BUNK! (Attendance will drop to " + String.format("%.2f%%", bunk1Pct) + " < 75%)\u001B[0m");
        }
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
    }

    private static void displayAllSubjectsBunkabilityDrop() {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-8s %-26s %-10s %-9s %-12s %-10s %-11s %-12s\n",
                "CODE", "SUBJECT NAME", "ATTENDED", "CURRENT %", "IF BUNK 1HR", "% DROP", "SAFE BUNKS", "BUNKABLE?");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        for (Subject s : subjectRegistry.values()) {
            double cur = s.getAttendancePercentage();
            double ifBunk = s.getProjectedPercentageIfBunk();
            double drop = cur - ifBunk;
            String bunkStatus = s.isBunkableNextHour() ? "\u001B[32m✔ YES\u001B[0m" : "\u001B[31m✖ NO\u001B[0m";

            System.out.printf("%-8s %-26s %-10s %-8.2f%% %-11.2f%% \u001B[31m-%5.2f%%\u001B[0m %-11d %-12s\n",
                    s.getSubjectCode(),
                    s.getSubjectName().length() > 26 ? s.getSubjectName().substring(0, 23) + "..." : s.getSubjectName(),
                    s.getAttendedClasses() + "/" + s.getTotalClasses(),
                    cur,
                    ifBunk,
                    drop,
                    s.getSafeBunks(),
                    bunkStatus
            );
        }
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Legacy queue processor for automated demo.
     */
    private static void processDailyScheduleQueue() {
        viewTodaysSchedule(null);
    }


    private static void displayWeeklyTimetableGrid(Scanner scanner) {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                         📅 WEEKLY CLASS TIMETABLE GRID                                      ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");

        if (scheduleManager.getWeeklySchedule().isEmpty()) {
            System.out.println("ℹ️ No scraped weekly timetable currently in memory.");
            System.out.println("Generating default semester timetable grid for preview...");
            TimetableParser.TimetableResult sampleTt = getSampleTimetableResult();
            scheduleManager.setWeeklySchedule(sampleTt.getScheduleByDay());
        }

        TimetableParser.TimetableResult tt = new TimetableParser.TimetableResult();
        for (var entry : scheduleManager.getWeeklySchedule().entrySet()) {
            for (ClassSession s : entry.getValue()) {
                tt.addSession(entry.getKey(), s);
            }
        }
        System.out.println(tt.formatWeeklyTable());
    }

    private static void initDailyQueue() {
        scheduleManager.clear();
        scheduleManager.addSession(new ClassSession(1, "09:00 - 10:00", "CS203", "Data Structures & Algorithms", "LH-101"));
        scheduleManager.addSession(new ClassSession(2, "10:00 - 11:00", "CS207", "Digital Electronics", "LH-101"));
        scheduleManager.addSession(new ClassSession(3, "11:15 - 12:15", "CS205", "Java Programming", "CC-Lab 2"));
        scheduleManager.addSession(new ClassSession(4, "01:15 - 02:15", "CS201", "Discrete Mathematics", "LH-101"));
        scheduleManager.addSession(new ClassSession(5, "02:15 - 03:15", "EST200", "Design & Engineering", "LH-104"));
    }

    /**
     * Feature 4: What-If Simulator (CustomStack LIFO Undo/Redo).
     */
    private static void launchWhatIfSimulator(Scanner scanner) {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                🔮 'WHAT-IF' ATTENDANCE SIMULATOR (CustomStack Undo/Redo)                   ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("Simulate bunking or attending future lectures to see real-time impact on your percentage.");
        System.out.println("Every simulated action is pushed onto the Stack so you can freely UNDO and REDO!");

        boolean inSimulator = true;
        while (inSimulator) {
            System.out.println("\nAvailable Subjects: " + subjectRegistry.keySet());
            System.out.printf("Actions in Undo Stack: %d | Actions in Redo Stack: %d\n",
                    whatIfSimulator.getUndoCount(), whatIfSimulator.getRedoCount());
            System.out.println("Options:");
            System.out.println("  [B] Bunk a Subject Class");
            System.out.println("  [A] Attend a Subject Class");
            System.out.println("  [U] Undo Last Action (Pop from Stack)");
            System.out.println("  [R] Redo Action");
            System.out.println("  [D] View Current Attendance Dashboard");
            System.out.println("  [Q] Return to Main Menu");
            System.out.print("👉 Choose simulation action: ");
            String opt = scanner.nextLine().trim().toUpperCase();

            switch (opt) {
                case "B": {
                    System.out.print("Enter Subject Code to BUNK: ");
                    String code = scanner.nextLine().trim().toUpperCase();
                    Subject s = subjectRegistry.get(code);
                    if (s == null) {
                        System.out.println("❌ Subject code not found!");
                        break;
                    }
                    double beforePct = s.getAttendancePercentage();
                    SimulationAction action = whatIfSimulator.simulateBunk(s, "Manual simulation test");
                    dangerZoneService.rebuildTree(subjectRegistry.values());
                    System.out.printf("\u001B[33mSimulated BUNK for %s: Attendance changed from %.2f%% to %.2f%%\u001B[0m\n",
                            code, beforePct, s.getAttendancePercentage());
                    System.out.println("Current status: " + s);
                    break;
                }
                case "A": {
                    System.out.print("Enter Subject Code to ATTEND: ");
                    String code = scanner.nextLine().trim().toUpperCase();
                    Subject s = subjectRegistry.get(code);
                    if (s == null) {
                        System.out.println("❌ Subject code not found!");
                        break;
                    }
                    double beforePct = s.getAttendancePercentage();
                    SimulationAction action = whatIfSimulator.simulateAttend(s, "Manual simulation test");
                    dangerZoneService.rebuildTree(subjectRegistry.values());
                    System.out.printf("\u001B[32mSimulated ATTEND for %s: Attendance changed from %.2f%% to %.2f%%\u001B[0m\n",
                            code, beforePct, s.getAttendancePercentage());
                    System.out.println("Current status: " + s);
                    break;
                }
                case "U": {
                    if (!whatIfSimulator.canUndo()) {
                        System.out.println("⚠️ Nothing to undo! Stack is empty.");
                        break;
                    }
                    SimulationAction undone = whatIfSimulator.undo();
                    dangerZoneService.rebuildTree(subjectRegistry.values());
                    System.out.printf("↩️ Undone action: %s on %s. Restored to: %s\n",
                            undone.getType(), undone.getSubject().getSubjectCode(), undone.getSubject());
                    break;
                }
                case "R": {
                    if (!whatIfSimulator.canRedo()) {
                        System.out.println("⚠️ Nothing to redo! Redo stack is empty.");
                        break;
                    }
                    SimulationAction redone = whatIfSimulator.redo();
                    dangerZoneService.rebuildTree(subjectRegistry.values());
                    System.out.printf("🔁 Redone action: %s on %s. Current: %s\n",
                            redone.getType(), redone.getSubject().getSubjectCode(), redone.getSubject());
                    break;
                }
                case "D":
                    displayAttendanceDashboard();
                    break;
                case "Q":
                    inSimulator = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Feature 5: Academic Calendar (Doubly Linked List).
     */
    private static void displayAcademicCalendarDLL() {
        displayAcademicCalendarDLL(null);
    }

    private static void displayAcademicCalendarDLL(Scanner scanner) {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("     📅 AJCE ACADEMIC CALENDAR & SEMESTER-END PROJECTIONS (Doubly Linked List Traversal)    ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");

        LocalDate today = LocalDate.of(2026, 10, 26);
        int workingDaysLeft = calendarService.countWorkingDaysRemaining(calendarManager, today);
        int workingDaysElapsed = calendarService.countWorkingDaysElapsed(calendarManager, today);
        int totalWorkingDays = calendarService.getTotalInstructionalWorkingDays();

        System.out.println("🌐 Academic Calendar Source : " + calendarService.getDataSource());
        System.out.println("🔗 Official Calendar Link   : " + AcademicCalendarService.AJCE_CALENDAR_PAGE_URL);
        System.out.printf("📆 Reference Date (Today)   : \u001B[33m%s\u001B[0m | Semester: S3 (Higher Semesters)\n", today);
        System.out.printf("📊 Working Days Summary     : \u001B[32m%d Elapsed\u001B[0m / %d Total | \u001B[36m%d Working Days Left\u001B[0m (%.1f%% complete)\n",
                workingDaysElapsed, totalWorkingDays, workingDaysLeft, ((double) workingDaysElapsed / totalWorkingDays) * 100.0);
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        System.out.println("🎯 KEY SEMESTER MILESTONES & EXAM COUNTDOWN (DLL Traversal):");
        LocalDate lastInst = calendarService.getLastInstructionalDay();
        int daysUntilLastInst = calendarManager.countDaysUntilExam(lastInst, today);
        int workingDaysUntilLastInst = calendarManager.countInstructionalWorkingDaysRemaining(today, lastInst);
        System.out.printf("  🏁 Last Instructional Day : %s -> \u001B[32m%d working days\u001B[0m (%d calendar days)\n",
                lastInst, workingDaysUntilLastInst, daysUntilLastInst);

        LocalDate condonationStart = calendarService.getCondonationStartDate();
        System.out.printf("  📋 Attendance & Condonation : %s (Apply if attendance < 75%%)\n", condonationStart);

        LocalDate condonationNoFine = calendarService.getCondonationDeadlineNoFine();
        int daysUntilCondonation = calendarManager.countDaysUntilExam(condonationNoFine, today);
        System.out.printf("  ⚠️ Condonation Deadline    : %s (Without fine) -> %d calendar days left\n",
                condonationNoFine, daysUntilCondonation);

        LocalDate examDate = calendarService.getFirstExamDate();
        int daysUntilExam = calendarManager.countDaysUntilExam(examDate, today);
        System.out.printf("  📚 End Semester Exam 1     : %s -> \u001B[35m%d calendar days left\u001B[0m\n",
                examDate, daysUntilExam);
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        System.out.println("📈 SEMESTER-END ATTENDANCE & BUNK BUDGET PROJECTIONS (Accurate to Calendar):");
        System.out.printf("%-8s %-26s %-10s %-8s %-10s %-9s %-11s %-12s\n",
                "CODE", "SUBJECT NAME", "ATTENDED", "CURR %", "CLASSES REM", "MAX %", "MUST ATTEND", "BUNK BUDGET");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        for (Subject s : subjectRegistry.values()) {
            SemesterProjection proj = SemesterAttendanceProjector.project(
                    s, today, calendarManager, calendarService, scheduleManager);

            String maxPctStr = String.format("%.1f%%", proj.getMaxPossiblePercentage());
            if (!proj.isCanAchieveThreshold()) {
                maxPctStr = "\u001B[31m" + maxPctStr + " ✖\u001B[0m";
            } else {
                maxPctStr = "\u001B[32m" + maxPctStr + " ✔\u001B[0m";
            }

            String budgetStr = proj.getSemesterBunksRemaining() > 0
                    ? "\u001B[32m" + proj.getSemesterBunksRemaining() + " bunks\u001B[0m"
                    : "\u001B[31m0 bunks\u001B[0m";

            System.out.printf("%-8s %-26s %-10s %-8.1f%% %-10d %-18s %-11d %-12s\n",
                    s.getSubjectCode(),
                    s.getSubjectName().length() > 26 ? s.getSubjectName().substring(0, 23) + "..." : s.getSubjectName(),
                    s.getAttendedClasses() + "/" + s.getTotalClasses(),
                    s.getAttendancePercentage(),
                    proj.getRemainingClasses(),
                    maxPctStr,
                    proj.getMustAttendToPass(),
                    budgetStr
            );

            if (!proj.isCanAchieveThreshold()) {
                System.out.printf("  🚨 \u001B[31m[CONDONATION ALERT] Cannot reach 75%%! Max possible is %.1f%%. Submit medical certificate before %s!\u001B[0m\n",
                        proj.getMaxPossiblePercentage(), condonationNoFine);
            }
        }
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");

        if (scanner != null) {
            System.out.println("\nOptions: [1] View Remaining Working Days Schedule | [2] Live Sync from AJCE | [Enter] Back");
            System.out.print("👉 Choice: ");
            String opt = scanner.nextLine().trim();
            if (opt.equals("1")) {
                System.out.println("\n📅 Remaining Instructional Working Days (Forward Traversal):");
                List<CalendarDay> remDays = calendarService.getRemainingInstructionalDays(calendarManager, today);
                for (CalendarDay d : remDays) {
                    System.out.println("  " + d);
                }
            } else if (opt.equals("2")) {
                System.out.println("🔄 Syncing calendar live from official AJCE Google Sheet...");
                boolean ok = calendarService.loadCalendar(calendarManager, true, false);
                if (ok) {
                    System.out.println("\u001B[32m✔ Calendar successfully synchronized live from AJCE!\u001B[0m");
                } else {
                    System.out.println("\u001B[31m✖ Could not sync live; using offline cache.\u001B[0m");
                }
            }
        }
    }

    /**
     * Feature 6: Custom Attendance Input.
     */
    private static void inputCustomSubjectData(Scanner scanner) {
        System.out.println("\n✍️ Input / Update Attendance Data");
        try {
            System.out.print("Enter Subject Code (e.g., CS301): ");
            String code = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter Subject Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter Classes Attended: ");
            int attended = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Total Classes Conducted: ");
            int total = Integer.parseInt(scanner.nextLine().trim());

            Subject subject = new Subject(code, name, attended, total);
            subjectRegistry.put(code, subject);
            dangerZoneService.rebuildTree(subjectRegistry.values());

            System.out.println("\u001B[32m✔ Subject successfully added/updated!\u001B[0m");
            System.out.println("Current Status: " + subject);
        } catch (Exception e) {
            System.out.println("❌ Error adding subject: " + e.getMessage());
        }
    }

    /**
     * Feature 7: Automated Walkthrough demonstrating all DS requirements.
     */
    private static void runAutomatedWalkthrough() {
        System.out.println("\n========================================================================");
        System.out.println("       🚀 EXECUTING AUTOMATED CANIBUNK.COM PHASE 1 DEMONSTRATION        ");
        System.out.println("========================================================================");

        System.out.println("\n--- Step 1: Attendance Dashboard (CustomHashMap O(1) Lookup) ---");
        displayAttendanceDashboard();

        System.out.println("\n--- Step 2: Danger Zone Sorting (AttendanceBST In-Order Traversal) ---");
        displayDangerZoneBST();

        System.out.println("\n--- Step 3: Daily Schedule Queue (CustomQueue FIFO) ---");
        processDailyScheduleQueue();
        System.out.println("\nProcessing second hour in queue...");
        processDailyScheduleQueue();

        System.out.println("\n--- Step 4: What-If Attendance Simulator (CustomStack Undo/Redo) ---");
        Subject testSub = subjectRegistry.get("CS207"); // 14/20 = 70% (Danger)
        System.out.println("Selected Subject: " + testSub);
        System.out.println("Simulating ATTENDING 3 consecutive classes...");
        whatIfSimulator.simulateAttend(testSub, "Attended Mon");
        whatIfSimulator.simulateAttend(testSub, "Attended Tue");
        whatIfSimulator.simulateAttend(testSub, "Attended Wed");
        System.out.println("After 3 Attends: " + testSub);
        System.out.printf("Attendance jumped to: %.2f%% (Safe Bunks now: %d)\n",
                testSub.getAttendancePercentage(), testSub.getSafeBunks());

        System.out.println("\nTesting UNDO using CustomStack (popping last action)...");
        whatIfSimulator.undo();
        System.out.println("After 1 Undo: " + testSub);
        whatIfSimulator.undo();
        whatIfSimulator.undo();
        System.out.println("After 3 Undos (reverted to original): " + testSub);

        System.out.println("\n--- Step 5: Academic Calendar (Doubly Linked List) ---");
        displayAcademicCalendarDLL();

        System.out.println("\n========================================================================");
        System.out.println("✔ AUTOMATED DEMO COMPLETE: All DS Concepts & Math Formulas Validated!");
        System.out.println("========================================================================");
    }

    /**
     * Feature 8: Live Selenium Scraper (aesajce.in).
     * Connects via headless browser, logs in, directly accesses:
     *   - https://www.aesajce.in/students/currentAttendance.php (Subject-wise Attendance)
     *   - https://www.aesajce.in/students/classworkSchedule.php (Class Timetable)
     * Populates both CustomHashMap and ScheduleManager's CustomQueue.
     */
    private static void runLiveSeleniumScraper(Scanner scanner) {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("              🌐 LIVE COLLEGE PORTAL SCRAPER (aesajce.in via Selenium WebDriver)            ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("Direct Targets:");
        System.out.println("  1. Subject Attendance : " + AesPortalScraper.ATTENDANCE_URL);
        System.out.println("  2. Class Timetable    : " + AesPortalScraper.TIMETABLE_URL);
        System.out.println("Security: Credentials are used exclusively for this session and discarded immediately.\n");

        try {
            System.out.print("👉 Enter College Admission Number / Username: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("❌ Username cannot be empty.");
                return;
            }

            char[] password;
            if (System.console() != null) {
                password = System.console().readPassword("👉 Enter AES Password (masked): ");
            } else {
                System.out.print("👉 Enter AES Password: ");
                password = scanner.nextLine().trim().toCharArray();
            }

            if (password == null || password.length == 0) {
                System.out.println("❌ Password cannot be empty.");
                return;
            }

            System.out.print("👉 Run browser in headless mode? (Y/n - choose 'n' to watch browser automate): ");
            String hlInput = scanner.nextLine().trim().toLowerCase();
            boolean headless = !hlInput.equals("n");

            AesPortalScraper scraper = new AesPortalScraper(headless);
            System.out.println("\n🚀 Launching Selenium WebDriver session...");
            AesPortalScraper.ScraperResult result = scraper.scrapePortal(username, password, subjectRegistry, scheduleManager);

            if (result.getSubjectCount() > 0) {
                dangerZoneService.rebuildTree(subjectRegistry.values());
                System.out.printf("\n\u001B[32m✔ SUCCESS! Scraped %d subjects directly from currentAttendance.php!\u001B[0m\n", result.getSubjectCount());
                if (result.getTimetableSessionsCount() > 0) {
                    System.out.printf("\u001B[32m✔ SUCCESS! Scraped class timetable from classworkSchedule.php (%d sessions loaded into today's queue)!\u001B[0m\n",
                            result.getTimetableSessionsCount());
                }
                displayAttendanceDashboard();
            } else {
                System.out.println("⚠️ Scraper completed but could not extract subjects. Please verify portal structure or credentials.");
            }

        } catch (Exception e) {
            System.out.println("\u001B[31m❌ Scraper Error: " + e.getMessage() + "\u001B[0m");
            System.out.println("Tip: Check 'scraped_attendance_debug.html' & 'scraped_timetable_debug.html', or use Option 9 (Sample HTML Scraper).");
        }
    }

    /**
     * Feature 9: Offline / Sample HTML Scraper Demo.
     * Demonstrates parsing both realistic AES attendance table (currentAttendance.php format)
     * and class timetable grid (classworkSchedule.php format) directly into CustomHashMap & CustomQueue.
     * If live debug files exist, automatically loads them for an instant offline preview!
     */
    private static void runOfflineScraperDemo() {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("               🧪 OFFLINE / SAMPLE HTML TABLE SCRAPER DEMONSTRATION                         ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");

        java.nio.file.Path attPath = java.nio.file.Path.of("scraped_attendance_debug.html");
        java.nio.file.Path ttPath = java.nio.file.Path.of("scraped_timetable_debug.html");

        String attendanceHtml = null;
        String timetableHtml = null;

        if (java.nio.file.Files.exists(attPath)) {
            try {
                attendanceHtml = java.nio.file.Files.readString(attPath);
                System.out.println("📁 Found live attendance snapshot: 'scraped_attendance_debug.html' on disk.");
            } catch (Exception ignored) {}
        }

        if (java.nio.file.Files.exists(ttPath)) {
            try {
                timetableHtml = java.nio.file.Files.readString(ttPath);
                System.out.println("📁 Found live timetable snapshot: 'scraped_timetable_debug.html' on disk.");
            } catch (Exception ignored) {}
        }

        if (attendanceHtml == null) {
            attendanceHtml =
                "<table class=\"table table-bordered table-striped\">\n" +
                "  <thead>\n" +
                "    <tr><th>Sl.No</th><th>Course Code</th><th>Course Title</th><th>TH</th><th>AH</th><th>DL</th><th>AH+DL</th><th>%AH</th><th>%AH+DL</th></tr>\n" +
                "  </thead>\n" +
                "  <tbody>\n" +
                "    <tr><td>1</td><td>CST201</td><td>Discrete Mathematical Structures</td><td>36</td><td>32</td><td>2</td><td>34</td><td>88.89</td><td>94.44</td></tr>\n" +
                "    <tr><td>2</td><td>CST203</td><td>Logic System Design</td><td>30</td><td>22</td><td>1</td><td>23</td><td>73.33</td><td>76.67</td></tr>\n" +
                "    <tr><td>3</td><td>CST205</td><td>Object Oriented Programming (Java)</td><td>28</td><td>25</td><td>0</td><td>25</td><td>89.29</td><td>89.29</td></tr>\n" +
                "    <tr><td>4</td><td>CST207</td><td>Data Structures & Algorithms</td><td>32</td><td>24</td><td>0</td><td>24</td><td>75.00</td><td>75.00</td></tr>\n" +
                "    <tr><td>5</td><td>EST200</td><td>Design & Engineering</td><td>20</td><td>12</td><td>1</td><td>13</td><td>60.00</td><td>65.00</td></tr>\n" +
                "  </tbody>\n" +
                "</table>";
        }

        if (timetableHtml == null) {
            timetableHtml =
                "<table class=\"table table-bordered table-striped text-center\">\n" +
                "  <thead>\n" +
                "    <tr>\n" +
                "      <th>Day / Hour</th>\n" +
                "      <th>1<br>09:00 - 10:00</th>\n" +
                "      <th>2<br>10:00 - 11:00</th>\n" +
                "      <th>3<br>11:15 - 12:15</th>\n" +
                "      <th>4<br>01:15 - 02:15</th>\n" +
                "      <th>5<br>02:15 - 03:15</th>\n" +
                "      <th>6<br>03:15 - 04:15</th>\n" +
                "    </tr>\n" +
                "  </thead>\n" +
                "  <tbody>\n" +
                "    <tr><td><b>Monday</b></td><td>CST201 (LH-101)</td><td>CST203 (LH-101)</td><td>CST205 (CC-Lab 2)</td><td>CST207 (LH-101)</td><td>EST200 (LH-104)</td><td>-</td></tr>\n" +
                "    <tr><td><b>Tuesday</b></td><td>CST203 (LH-101)</td><td>CST207 (LH-101)</td><td>CST205 (CC-Lab 2)</td><td>CST201 (LH-101)</td><td>EST200 (LH-104)</td><td>-</td></tr>\n" +
                "    <tr><td><b>Wednesday</b></td><td>CST205 (CC-Lab 2)</td><td>CST201 (LH-101)</td><td>CST203 (LH-101)</td><td>CST207 (LH-101)</td><td>CST201 (LH-101)</td><td>-</td></tr>\n" +
                "    <tr><td><b>Thursday</b></td><td>CST207 (LH-101)</td><td>CST205 (CC-Lab 2)</td><td>CST201 (LH-101)</td><td>CST203 (LH-101)</td><td>EST200 (LH-104)</td><td>-</td></tr>\n" +
                "    <tr><td><b>Friday</b></td><td>CST201 (LH-101)</td><td>CST205 (CC-Lab 2)</td><td>CST207 (LH-101)</td><td>CST203 (LH-101)</td><td>-</td><td>-</td></tr>\n" +
                "  </tbody>\n" +
                "</table>";
        }

        System.out.println("\n--- [1] Parsing Attendance Table HTML ---");
        List<Subject> parsedSubjects = AttendanceTableParser.parseHtmlTable(attendanceHtml);
        int subCount = AttendanceTableParser.populateRegistry(parsedSubjects, subjectRegistry);
        dangerZoneService.rebuildTree(subjectRegistry.values());
        System.out.printf("\u001B[32m✔ Successfully loaded %d subjects into CustomHashMap!\u001B[0m\n", subCount);

        System.out.println("\n--- [2] Parsing Class Timetable HTML ---");
        TimetableParser.TimetableResult ttResult = TimetableParser.parseTimetable(timetableHtml, subjectRegistry);
        scheduleManager.setWeeklySchedule(ttResult.getScheduleByDay());
        int queueCount = TimetableParser.populateTodaySchedule(ttResult, scheduleManager, subjectRegistry);
        System.out.printf("\u001B[32m✔ Successfully loaded %d periods into today's Schedule Queue (CustomQueue FIFO)!\u001B[0m\n", queueCount);

        displayAttendanceDashboard();
        displayDangerZoneBST();
        System.out.println(ttResult.formatWeeklyTable());
    }


    private static TimetableParser.TimetableResult getSampleTimetableResult() {
        TimetableParser.TimetableResult tt = new TimetableParser.TimetableResult();
        tt.addSession("MONDAY", new ClassSession(1, "09:00 - 10:00", "CS203", "Data Structures & Algorithms", "LH-101"));
        tt.addSession("MONDAY", new ClassSession(2, "10:00 - 11:00", "CS207", "Digital Electronics", "LH-101"));
        tt.addSession("MONDAY", new ClassSession(3, "11:15 - 12:15", "CS205", "Java Programming", "CC-Lab 2"));
        tt.addSession("MONDAY", new ClassSession(4, "01:15 - 02:15", "CS201", "Discrete Mathematics", "LH-101"));
        tt.addSession("MONDAY", new ClassSession(5, "02:15 - 03:15", "EST200", "Design & Engineering", "LH-104"));

        tt.addSession("TUESDAY", new ClassSession(1, "09:00 - 10:00", "CS201", "Discrete Mathematics", "LH-101"));
        tt.addSession("TUESDAY", new ClassSession(2, "10:00 - 11:00", "CS205", "Java Programming", "CC-Lab 2"));
        tt.addSession("TUESDAY", new ClassSession(3, "11:15 - 12:15", "CS203", "Data Structures & Algorithms", "LH-101"));
        tt.addSession("TUESDAY", new ClassSession(4, "01:15 - 02:15", "CS207", "Digital Electronics", "LH-101"));
        tt.addSession("TUESDAY", new ClassSession(5, "02:15 - 03:15", "EST200", "Design & Engineering", "LH-104"));
        return tt;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}

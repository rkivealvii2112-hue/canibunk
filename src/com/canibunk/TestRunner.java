package com.canibunk;

import com.canibunk.ds.*;
import com.canibunk.model.*;
import com.canibunk.service.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated test suite to verify math accuracy, boundary conditions, and custom Data Structures.
 */
public class TestRunner {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("      CanIBunk.com - Phase 1 Verification Test Suite           ");
        System.out.println("===============================================================");

        testAttendanceMath();
        testCustomStack();
        testCustomQueue();
        testDoublyLinkedList();
        testAttendanceBST();
        testCustomHashMap();
        testWhatIfSimulator();
        testHtmlTableParser();
        testAcademicCalendarAndSemesterProjections();

        System.out.println("---------------------------------------------------------------");
        System.out.printf("Test Results: %d Passed, %d Failed.\n", testsPassed, testsFailed);
        if (testsFailed == 0) {
            System.out.println("\u001B[32m✔ ALL CORE LOGIC & DATA STRUCTURE TESTS PASSED!\u001B[0m");
        } else {
            System.out.println("\u001B[31m✖ SOME TESTS FAILED. Check logs above.\u001B[0m");
            System.exit(1);
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            testsPassed++;
            System.out.printf("  [PASS] %s\n", testName);
        } else {
            testsFailed++;
            System.out.printf("  \u001B[31m[FAIL] %s -> Expected: %s, Got: %s\u001B[0m\n", testName, expected, actual);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] %s\n", testName);
        } else {
            testsFailed++;
            System.out.printf("  \u001B[31m[FAIL] %s -> Expected: true, Got: false\u001B[0m\n", testName);
        }
    }

    private static void testAttendanceMath() {
        System.out.println("\n[1] Testing Attendance Math Formulas & Edge Cases...");

        // Case 1: 18/20 = 90%. Safe bunks: (18 / 0.75) - 20 = 24 - 20 = 4.
        double pct1 = AttendanceCalculator.calculatePercentage(18, 20);
        assertEquals("Percentage 18/20 is 90.0%", 90.0, pct1);
        int bunks1 = AttendanceCalculator.calculateSafeBunks(18, 20);
        assertEquals("Safe bunks for 18/20 (90%) is 4", 4, bunks1);
        assertTrue("Next hour bunkable for 18/20 (90%)", AttendanceCalculator.isBunkableNextHour(18, 20));

        // Case 2: 8/10 = 80%. Safe bunks: (8 / 0.75) - 10 = 10.666 - 10 = 0.666 -> 0 bunks!
        // If bunked: 8/11 = 72.72% < 75%.
        int bunks2 = AttendanceCalculator.calculateSafeBunks(8, 10);
        assertEquals("Safe bunks for 8/10 (80%) is 0 (Crucial Edge Case)", 0, bunks2);
        assertTrue("Next hour is NOT bunkable for 8/10 (80%)", !AttendanceCalculator.isBunkableNextHour(8, 10));

        // Case 3: 14/20 = 70%. Below 75%. Classes needed: ceil((0.75 * 20 - 14) / 0.25) = (15 - 14)/0.25 = 4.
        int needed1 = AttendanceCalculator.calculateClassesNeeded(14, 20);
        assertEquals("Classes needed for 14/20 (70%) to reach 75% is 4", 4, needed1);
        assertEquals("Safe bunks for 14/20 is 0", 0, AttendanceCalculator.calculateSafeBunks(14, 20));
        assertTrue("Next hour is NOT bunkable when in danger zone (70%)", !AttendanceCalculator.isBunkableNextHour(14, 20));

        // Case 4: 13/20 = 65%. Needed: ceil((15 - 13) / 0.25) = 8.
        int needed2 = AttendanceCalculator.calculateClassesNeeded(13, 20);
        assertEquals("Classes needed for 13/20 (65%) to reach 75% is 8", 8, needed2);

        // Case 5: Exactly 15/20 = 75%. Safe bunks = floor((15/0.75) - 20) = 20 - 20 = 0.
        // If bunk 1: 15/21 = 71.4% < 75. So 0 bunks, needed 0.
        assertEquals("Safe bunks for exact 75% is 0", 0, AttendanceCalculator.calculateSafeBunks(15, 20));
        assertEquals("Classes needed for exact 75% is 0", 0, AttendanceCalculator.calculateClassesNeeded(15, 20));
        assertTrue("Next hour NOT bunkable at exact 75%", !AttendanceCalculator.isBunkableNextHour(15, 20));
    }

    private static void testCustomStack() {
        System.out.println("\n[2] Testing CustomStack (LIFO Dynamic Array)...");
        CustomStack<String> stack = new CustomStack<>(2); // small capacity to test resize
        assertTrue("Initial stack is empty", stack.isEmpty());
        stack.push("Action1");
        stack.push("Action2");
        stack.push("Action3"); // triggers resize
        assertEquals("Stack size is 3", 3, stack.size());
        assertEquals("Peek is Action3", "Action3", stack.peek());
        assertEquals("Pop is Action3", "Action3", stack.pop());
        assertEquals("Pop is Action2", "Action2", stack.pop());
        assertEquals("Pop is Action1", "Action1", stack.pop());
        assertTrue("Stack is empty after pops", stack.isEmpty());
    }

    private static void testCustomQueue() {
        System.out.println("\n[3] Testing CustomQueue (FIFO Circular Buffer)...");
        CustomQueue<Integer> queue = new CustomQueue<>(3); // small capacity for circular test
        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);
        assertEquals("Queue size is 3", 3, queue.size());
        assertEquals("Peek is 10", Integer.valueOf(10), queue.peek());
        assertEquals("Dequeue is 10", Integer.valueOf(10), queue.dequeue());
        // Enqueue to test circular wrap-around
        queue.enqueue(40);
        assertEquals("Dequeue is 20", Integer.valueOf(20), queue.dequeue());
        assertEquals("Dequeue is 30", Integer.valueOf(30), queue.dequeue());
        assertEquals("Dequeue is 40", Integer.valueOf(40), queue.dequeue());
        assertTrue("Queue is empty", queue.isEmpty());
    }

    private static void testDoublyLinkedList() {
        System.out.println("\n[4] Testing DoublyLinkedList (Bidirectional Traversal)...");
        DoublyLinkedList<String> dll = new DoublyLinkedList<>();
        dll.addLast("Day1");
        dll.addLast("Day2");
        dll.addLast("Day3");

        assertEquals("DLL size is 3", 3, dll.size());
        assertEquals("Head is Day1", "Day1", dll.getHead().getData());
        assertEquals("Tail is Day3", "Day3", dll.getTail().getData());
        assertEquals("Head next is Day2", "Day2", dll.getHead().getNext().getData());
        assertEquals("Tail prev is Day2", "Day2", dll.getTail().getPrev().getData());
    }

    private static void testAttendanceBST() {
        System.out.println("\n[5] Testing AttendanceBST (Danger Sorting by Attendance %)...");
        AttendanceBST bst = new AttendanceBST();
        Subject s1 = new Subject("CS301", "Data Structures", 18, 20); // 90%
        Subject s2 = new Subject("CS302", "Java Programming", 13, 20); // 65%
        Subject s3 = new Subject("CS303", "Operating Systems", 15, 20); // 75%
        Subject s4 = new Subject("CS304", "Mathematics", 12, 20); // 60%

        bst.insert(s1);
        bst.insert(s2);
        bst.insert(s3);
        bst.insert(s4);

        List<Subject> sorted = bst.inOrderTraversal();
        assertEquals("BST size is 4", 4, sorted.size());
        // In-Order traversal should be sorted ascending: 60% (CS304), 65% (CS302), 75% (CS303), 90% (CS301)
        assertEquals("First subject in sorted order is lowest % (CS304)", "CS304", sorted.get(0).getSubjectCode());
        assertEquals("Second subject is CS302", "CS302", sorted.get(1).getSubjectCode());
        assertEquals("Third subject is CS303", "CS303", sorted.get(2).getSubjectCode());
        assertEquals("Last subject is highest % (CS301)", "CS301", sorted.get(3).getSubjectCode());

        assertEquals("Most Critical Subject is CS304", "CS304", bst.getMostCriticalSubject().getSubjectCode());
        assertEquals("Safest Subject is CS301", "CS301", bst.getSafestSubject().getSubjectCode());

        List<Subject> dangerList = bst.getDangerZoneSubjects(75.0);
        assertEquals("2 subjects are strictly below 75%", 2, dangerList.size());
    }

    private static void testCustomHashMap() {
        System.out.println("\n[6] Testing CustomHashMap (Separate Chaining Lookup)...");
        CustomHashMap<String, Subject> map = new CustomHashMap<>(4);
        Subject s1 = new Subject("CS301", "Data Structures", 18, 20);
        Subject s2 = new Subject("CS302", "Java Programming", 14, 20);

        map.put("CS301", s1);
        map.put("CS302", s2);

        assertEquals("Map size is 2", 2, map.size());
        assertEquals("Get CS301 returns s1", s1, map.get("CS301"));
        assertEquals("Get CS302 returns s2", s2, map.get("CS302"));
        assertTrue("Contains CS301", map.containsKey("CS301"));
        assertTrue("Does not contain CS999", !map.containsKey("CS999"));
    }

    private static void testWhatIfSimulator() {
        System.out.println("\n[7] Testing What-If Simulator (Stack Undo/Redo)...");
        WhatIfSimulator simulator = new WhatIfSimulator();
        Subject s = new Subject("CS301", "Data Structures", 18, 20); // 90%, 4 bunks

        // Simulate bunking
        simulator.simulateBunk(s, "Skipping to study DS");
        assertEquals("After 1 bunk: attended remains 18", 18, s.getAttendedClasses());
        assertEquals("After 1 bunk: total becomes 21", 21, s.getTotalClasses());
        assertEquals("Bunks left drops from 4 to 3", 3, s.getSafeBunks());

        // Undo
        SimulationAction undone = simulator.undo();
        assertTrue("Undo executed successfully", undone != null);
        assertEquals("After undo: total restored to 20", 20, s.getTotalClasses());
        assertEquals("After undo: safe bunks restored to 4", 4, s.getSafeBunks());

        // Redo
        SimulationAction redone = simulator.redo();
        assertTrue("Redo executed successfully", redone != null);
        assertEquals("After redo: total becomes 21 again", 21, s.getTotalClasses());

        // Revert cleanly
        simulator.undo();
    }

    private static void testHtmlTableParser() {
        System.out.println("\n[8] Testing AttendanceTableParser (HTML Table -> CustomHashMap)...");

        // Realistic HTML table format as seen in college portals (Amal Jyothi AES)
        String sampleHtml = 
            "<table class=\"table table-striped attendance-table\">\n" +
            "  <thead>\n" +
            "    <tr><th>Sl.No</th><th>Course Code</th><th>Course Title</th><th>Attended</th><th>Conducted</th><th>Percentage</th></tr>\n" +
            "  </thead>\n" +
            "  <tbody>\n" +
            "    <tr><td>1</td><td>CS201</td><td>Discrete Mathematics</td><td>22</td><td>25</td><td>88.00%</td></tr>\n" +
            "    <tr><td>2</td><td>CS203</td><td>Data Structures</td><td>19</td><td>24</td><td>79.17%</td></tr>\n" +
            "    <tr><td>3</td><td>CS205</td><td>Java Programming</td><td>24 / 26</td><td>26</td><td>92.31%</td></tr>\n" +
            "    <tr><td>4</td><td>CS207</td><td>Digital Electronics</td><td>14</td><td>20</td><td>70.00%</td></tr>\n" +
            "  </tbody>\n" +
            "</table>";

        List<Subject> parsed = com.canibunk.scraper.AttendanceTableParser.parseHtmlTable(sampleHtml);
        assertEquals("Parsed 4 subjects from HTML table", 4, parsed.size());

        CustomHashMap<String, Subject> registry = new CustomHashMap<>();
        int count = com.canibunk.scraper.AttendanceTableParser.populateRegistry(parsed, registry);
        assertEquals("Loaded 4 subjects into CustomHashMap", 4, count);

        Subject cs201 = registry.get("CS201");
        assertTrue("CS201 present in CustomHashMap", cs201 != null);
        assertEquals("CS201 attended is 22", 22, cs201.getAttendedClasses());
        assertEquals("CS201 total is 25", 25, cs201.getTotalClasses());
        assertEquals("CS201 safe bunks is 4", 4, cs201.getSafeBunks());

        Subject cs205 = registry.get("CS205");
        assertTrue("CS205 present in CustomHashMap", cs205 != null);
        assertEquals("CS205 attended is 24", 24, cs205.getAttendedClasses());
        assertEquals("CS205 total is 26", 26, cs205.getTotalClasses());

        Subject cs207 = registry.get("CS207");
        assertTrue("CS207 present in CustomHashMap", cs207 != null);
        assertEquals("CS207 attended is 14", 14, cs207.getAttendedClasses());
        assertEquals("CS207 classes needed to 75% is 4", 4, cs207.getClassesNeededTo75());

        // Test AES Amal Jyothi exact portal layout (TH, AH, DL, AH+DL)
        System.out.println("\n[9] Testing exact Amal Jyothi AES Portal Table Format (TH, AH, DL, AH+DL)...");
        String ajceAesHtml =
            "<table class=\"table table-bordered table-striped\">\n" +
            "  <thead>\n" +
            "    <tr><th>Sl.No</th><th>Course Code</th><th>Course Title</th><th>TH</th><th>AH</th><th>DL</th><th>AH+DL</th><th>%AH</th><th>%AH+DL</th></tr>\n" +
            "  </thead>\n" +
            "  <tbody>\n" +
            "    <tr><td>1</td><td>CST201</td><td>Discrete Mathematical Structures</td><td>36</td><td>32</td><td>2</td><td>34</td><td>88.89</td><td>94.44</td></tr>\n" +
            "    <tr><td>2</td><td>CST203</td><td>Logic System Design</td><td>30</td><td>22</td><td>1</td><td>23</td><td>73.33</td><td>76.67</td></tr>\n" +
            "    <tr><td>3</td><td>CST205</td><td>Object Oriented Programming</td><td>28</td><td>25</td><td>0</td><td>25</td><td>89.29</td><td>89.29</td></tr>\n" +
            "  </tbody>\n" +
            "</table>";

        List<Subject> aesParsed = com.canibunk.scraper.AttendanceTableParser.parseHtmlTable(ajceAesHtml);
        assertEquals("Parsed 3 subjects from AJCE AES table", 3, aesParsed.size());

        CustomHashMap<String, Subject> aesRegistry = new CustomHashMap<>();
        com.canibunk.scraper.AttendanceTableParser.populateRegistry(aesParsed, aesRegistry);

        Subject cst201 = aesRegistry.get("CST201");
        assertTrue("CST201 present", cst201 != null);
        assertEquals("CST201 Total (TH) is 36", 36, cst201.getTotalClasses());
        // Attended should use AH+DL (34)
        assertEquals("CST201 Attended (AH+DL) is 34", 34, cst201.getAttendedClasses());
        assertEquals("CST201 Safe bunks is 9", 9, cst201.getSafeBunks());

        Subject cst203 = aesRegistry.get("CST203");
        assertTrue("CST203 present", cst203 != null);
        assertEquals("CST203 Total (TH) is 30", 30, cst203.getTotalClasses());
        assertEquals("CST203 Attended (AH+DL) is 23", 23, cst203.getAttendedClasses());
        assertEquals("CST203 Safe bunks is 0", 0, cst203.getSafeBunks());

        // Test 10: Horizontal Table format (Amal Jyothi AES currentAttendance.php Subjectwise Attendance Percentage)
        System.out.println("\n[10] Testing Horizontal AES Portal Attendance Table (24MAT205, 24CST201, ...)...");
        String horizontalAesHtml =
            "<table class=\"table table-bordered\">\n" +
            "  <thead>\n" +
            "    <tr><th class=\"text-center\" colspan=\"8\">Attendance Percentage</th></tr>\n" +
            "  </thead>\n" +
            "  <tbody>\n" +
            "    <tr><th class=\"text-center\">24MAT205</th><th class=\"text-center\">24CST201</th><th class=\"text-center\">24CST203</th><th class=\"text-center\">24CST205</th><th class=\"text-center\">24MNC201</th><th class=\"text-center\">24CSL201</th><th class=\"text-center\">24CSL203</th><th class=\"text-center\">24HUT210</th></tr>\n" +
            "    <tr><td>91</td><td>90</td><td>94</td><td>91</td><td>94</td><td>100</td><td>91</td><td>95</td></tr>\n" +
            "  </tbody>\n" +
            "</table>";

        List<Subject> horizParsed = com.canibunk.scraper.AttendanceTableParser.parseHtmlTable(horizontalAesHtml);
        assertEquals("Parsed 8 subjects from horizontal AES table", 8, horizParsed.size());

        CustomHashMap<String, Subject> horizRegistry = new CustomHashMap<>();
        com.canibunk.scraper.AttendanceTableParser.populateRegistry(horizParsed, horizRegistry);

        Subject mat205 = horizRegistry.get("24MAT205");
        assertTrue("24MAT205 present", mat205 != null);
        assertEquals("24MAT205 percentage is 91%", 91.0, mat205.getAttendancePercentage());
        assertEquals("24MAT205 attended is 91", 91, mat205.getAttendedClasses());
        assertEquals("24MAT205 total is 100", 100, mat205.getTotalClasses());
        assertEquals("24MAT205 safe bunks is 21", 21, mat205.getSafeBunks());

        Subject csl201 = horizRegistry.get("24CSL201");
        assertTrue("24CSL201 present", csl201 != null);
        assertEquals("24CSL201 percentage is 100%", 100.0, csl201.getAttendancePercentage());

        // Test with live scraped_attendance_debug.html if present
        java.nio.file.Path debugPath = java.nio.file.Path.of("scraped_attendance_debug.html");
        if (java.nio.file.Files.exists(debugPath)) {
            try {
                String liveFileHtml = java.nio.file.Files.readString(debugPath);
                List<Subject> liveParsed = com.canibunk.scraper.AttendanceTableParser.parseHtmlTable(liveFileHtml);
                assertEquals("Parsed 8 subjects directly from scraped_attendance_debug.html", 8, liveParsed.size());
            } catch (Exception ignored) {}
        }
    }

    private static void testAcademicCalendarAndSemesterProjections() {
        System.out.println("\n[11] Testing Academic Calendar (AJCE S3) & Semester-End Projections...");
        CalendarManager cm = new CalendarManager();
        AcademicCalendarService calendarService = new AcademicCalendarService();

        boolean loaded = calendarService.loadCalendar(cm, false, false);
        assertTrue("Academic calendar loaded from offline cache", loaded);
        assertTrue("Calendar Doubly Linked List has at least 100 days", cm.getTotalDays() >= 100);

        assertEquals("Total S3 instructional working days is 80", 80, calendarService.getTotalInstructionalWorkingDays());
        assertEquals("Last instructional day is 2026-11-05", LocalDate.of(2026, 11, 5), calendarService.getLastInstructionalDay());
        assertEquals("Condonation deadline without fine is 2026-11-09", LocalDate.of(2026, 11, 9), calendarService.getCondonationDeadlineNoFine());
        assertEquals("End Semester Exam 1 is 2026-11-13", LocalDate.of(2026, 11, 13), calendarService.getFirstExamDate());

        LocalDate today = LocalDate.of(2026, 10, 26);
        int remWorkingDays = calendarService.countWorkingDaysRemaining(cm, today);
        assertEquals("Working days remaining as of 2026-10-26 is 9 days", 9, remWorkingDays);

        int remOnLastDay = calendarService.countWorkingDaysRemaining(cm, LocalDate.of(2026, 11, 5));
        assertEquals("Working days remaining on Last Instructional Day (2026-11-05) is 1", 1, remOnLastDay);

        int remAfterLastDay = calendarService.countWorkingDaysRemaining(cm, LocalDate.of(2026, 11, 6));
        assertEquals("Working days remaining after Last Instructional Day (2026-11-06) is 0", 0, remAfterLastDay);

        // Backward DLL traversal from Exam to Today
        LocalDate examDate = calendarService.getFirstExamDate();
        int daysUntilExam = cm.countDaysUntilExam(examDate, today);
        assertEquals("DLL backward traversal: Days from 2026-10-26 to ESE 1 (2026-11-13) is 18 days", 18, daysUntilExam);

        // Math: Case 1 - Safe Subject: 19/24, 5 classes remaining -> total = 29
        double maxPct1 = AttendanceCalculator.calculateMaxPossiblePercentage(19, 24, 5);
        assertTrue("Max possible % for 19/24 with 5 left is > 82.7%", Math.abs(maxPct1 - 82.758) < 0.01);
        assertTrue("Can reach 75% for 19/24 with 5 left", AttendanceCalculator.canReachThreshold(19, 24, 5));
        int mustAttend1 = AttendanceCalculator.calculateSemesterMustAttend(19, 24, 5);
        assertEquals("Must attend 3 of 5 classes (ceil(0.75*29) - 19 = 22 - 19 = 3)", 3, mustAttend1);
        int bunkBudget1 = AttendanceCalculator.calculateSemesterBunkBudget(19, 24, 5);
        assertEquals("Semester bunk budget is 2 (floor(0.25*29) - 5 = 7 - 5 = 2)", 2, bunkBudget1);

        // Math: Case 2 - Borderline Danger: 14/20 (70%), 4 classes remaining -> total = 24
        double maxPct2 = AttendanceCalculator.calculateMaxPossiblePercentage(14, 20, 4);
        assertEquals("Max possible % for 14/20 with 4 left is exactly 75.0%", 75.0, maxPct2);
        assertTrue("Can reach 75% for 14/20 with 4 left", AttendanceCalculator.canReachThreshold(14, 20, 4));
        int mustAttend2 = AttendanceCalculator.calculateSemesterMustAttend(14, 20, 4);
        assertEquals("Must attend ALL 4 remaining classes (18 - 14 = 4)", 4, mustAttend2);
        int bunkBudget2 = AttendanceCalculator.calculateSemesterBunkBudget(14, 20, 4);
        assertEquals("Semester bunk budget is 0 (cannot bunk any classes)", 0, bunkBudget2);

        // Math: Case 3 - Critical Condonation Shortage: 10/18 (55.5%), 4 classes remaining -> total = 22
        // Max attended = 14/22 = 63.64% < 75.0%
        double maxPct3 = AttendanceCalculator.calculateMaxPossiblePercentage(10, 18, 4);
        assertTrue("Max possible % for 10/18 with 4 left is < 64.0%", maxPct3 < 64.0);
        assertTrue("Cannot reach 75% for 10/18 with 4 left", !AttendanceCalculator.canReachThreshold(10, 18, 4));
        int bunkBudget3 = AttendanceCalculator.calculateSemesterBunkBudget(10, 18, 4);
        assertEquals("Bunk budget is 0 when shortage guaranteed", 0, bunkBudget3);

        // Test SemesterAttendanceProjector
        Subject subSafe = new Subject("CS203", "Data Structures", 19, 24);
        SemesterProjection projSafe = SemesterAttendanceProjector.project(subSafe, today, cm, calendarService, null);
        assertTrue("Projection can achieve 75%", projSafe.isCanAchieveThreshold());
        assertTrue("Projection has safe bunks", projSafe.getSemesterBunksRemaining() >= 0);

        Subject subShortage = new Subject("BAD101", "Severe Shortage", 10, 18);
        SemesterProjection projShortage = SemesterAttendanceProjector.project(subShortage, today, cm, calendarService, null);
        assertTrue("Projection correctly flags shortage", !projShortage.isCanAchieveThreshold());
        assertTrue("Verdict mentions Condonation", projShortage.getVerdict().contains("Condonation"));
    }
}



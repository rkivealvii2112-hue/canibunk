package com.canibunk.service;

import com.canibunk.ds.CustomHashMap;
import com.canibunk.ds.CustomQueue;
import com.canibunk.model.ClassSession;
import com.canibunk.model.Subject;

/**
 * Manages the Daily Class Schedule using CustomQueue (FIFO).
 * As each class hour approaches or passes, dequeue() is called to inspect
 * the upcoming lecture and determine live bunkability.
 */
public class ScheduleManager {
    private final CustomQueue<ClassSession> scheduleQueue;
    private java.util.Map<String, java.util.List<ClassSession>> weeklySchedule = new java.util.LinkedHashMap<>();
    private String activeDayName = "TODAY";

    public ScheduleManager() {
        this.scheduleQueue = new CustomQueue<>();
    }

    public void addSession(ClassSession session) {
        scheduleQueue.enqueue(session);
    }

    public void clear() {
        scheduleQueue.clear();
    }

    public void loadSessions(java.util.List<ClassSession> sessions) {
        clear();
        if (sessions != null) {
            for (ClassSession s : sessions) {
                scheduleQueue.enqueue(s);
            }
        }
    }

    public void setWeeklySchedule(java.util.Map<String, java.util.List<ClassSession>> schedule) {
        this.weeklySchedule = (schedule != null) ? schedule : new java.util.LinkedHashMap<>();
    }

    public java.util.Map<String, java.util.List<ClassSession>> getWeeklySchedule() {
        return weeklySchedule;
    }

    public java.util.List<ClassSession> getSessionsForDay(String dayName) {
        if (dayName == null || weeklySchedule == null) return java.util.Collections.emptyList();
        return weeklySchedule.getOrDefault(dayName.trim().toUpperCase(), java.util.Collections.emptyList());
    }

    public boolean loadDay(String dayName) {
        java.util.List<ClassSession> sessions = getSessionsForDay(dayName);
        if (!sessions.isEmpty()) {
            this.activeDayName = dayName.trim().toUpperCase();
            loadSessions(sessions);
            return true;
        }
        return false;
    }

    public String getActiveDayName() {
        return activeDayName;
    }

    public void setActiveDayName(String activeDayName) {
        this.activeDayName = activeDayName;
    }

    public boolean hasRemainingClasses() {
        return !scheduleQueue.isEmpty();
    }

    public int getRemainingClassesCount() {
        return scheduleQueue.size();
    }

    public ClassSession peekUpcomingClass() {
        if (scheduleQueue.isEmpty()) return null;
        return scheduleQueue.peek();
    }

    /**
     * Formats a complete daily timetable with side-by-side Skip/Bunk impact analysis.
     * Shows: Current %, If Attended %, If Skipped %, Exact % Decrease, Will it Drop < 75%, and Bunk Verdict.
     */
    public String formatDayScheduleAnalysis(String dayName, CustomHashMap<String, Subject> registry) {
        String targetDay = (dayName != null && !dayName.trim().isEmpty()) ? dayName.trim().toUpperCase() : activeDayName;
        java.util.List<ClassSession> sessions = getSessionsForDay(targetDay);

        if (sessions.isEmpty()) {
            sessions = getSessionsForDay("MONDAY");
            targetDay = "MONDAY (Fallback)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("                      📅 %s CLASS SCHEDULE & SKIP (BUNK) IMPACT ANALYSIS                                  \n", targetDay));
        sb.append("                      [Strict 75.0% Rule - Instant Evaluation for Every Single Class]                             \n");
        sb.append("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("%-3s | %-13s | %-32s | %-9s | %-9s | %-9s | %-9s | %-11s | %s\n",
                "Prd", "Time Slot", "Course Code & Name", "Current %", "If Attend", "If Skip", "Decrease", "Below 75%?", "Verdict"));
        sb.append("────┼───────────────┼──────────────────────────────────┼───────────┼───────────┼───────────┼───────────┼─────────────┼────────────────────────────\n");

        if (sessions.isEmpty()) {
            sb.append("  (No periods scheduled for this day)\n");
            sb.append("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
            return sb.toString();
        }

        for (ClassSession s : sessions) {
            Subject sub = (registry != null) ? registry.get(s.getSubjectCode()) : null;
            String codeAndName = "[" + s.getSubjectCode() + "] " + s.getSubjectName();
            if (codeAndName.length() > 32) {
                codeAndName = codeAndName.substring(0, 29) + "...";
            }

            if (sub == null) {
                // Non-academic or unrecorded period (e.g. Placement / Library / Free)
                sb.append(String.format("%-3d | %-13s | %-32s | %-9s | %-9s | %-9s | %-9s | %-11s | %s\n",
                        s.getPeriodNumber(),
                        s.getTimeSlot(),
                        codeAndName,
                        "--",
                        "--",
                        "--",
                        "--",
                        "NO",
                        "Non-academic / Free"));
            } else {
                double curPct = sub.getAttendancePercentage();
                double ifAttend = sub.getProjectedPercentageIfAttend();
                double ifBunk = sub.getProjectedPercentageIfBunk();
                double decrease = curPct - ifBunk;
                boolean willDropBelow = ifBunk < 75.0;
                int safeBunks = sub.getSafeBunks();
                int needed = sub.getClassesNeededTo75();

                String dropLabel = willDropBelow ? "\u001B[31mYES (<75%)\u001B[0m" : "\u001B[32mNO (>=75%)\u001B[0m";
                String verdict;

                if (ifBunk >= 75.0) {
                    verdict = String.format("\u001B[32m✔ SAFE (%d bunks left)\u001B[0m", Math.max(0, safeBunks - 1));
                } else if (curPct >= 75.0) {
                    verdict = "\u001B[33m⚠️ DANGER! DO NOT BUNK\u001B[0m";
                } else {
                    verdict = String.format("\u001B[31m✖ CRITICAL (Need %d)\u001B[0m", needed);
                }

                sb.append(String.format("%-3d | %-13s | %-32s | %8.2f%% | %8.2f%% | %8.2f%% | %8.2f%% | %-11s | %s\n",
                        s.getPeriodNumber(),
                        s.getTimeSlot(),
                        codeAndName,
                        curPct,
                        ifAttend,
                        ifBunk,
                        -decrease,
                        dropLabel,
                        verdict));
            }
        }

        sb.append("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
        return sb.toString();
    }

    /**
     * Dequeues the next class session and analyzes its bunkability based on current statistics.
     */
    public String evaluateAndAdvance(CustomHashMap<String, Subject> registry) {
        if (scheduleQueue.isEmpty()) {
            return "No more classes scheduled for today! You're free.";
        }

        ClassSession session = scheduleQueue.dequeue();
        Subject subject = (registry != null) ? registry.get(session.getSubjectCode()) : null;

        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append(String.format("Now Processing: Period %d [%s] - %s\n",
                session.getPeriodNumber(), session.getTimeSlot(), session.getSubjectName()));
        report.append(String.format("Room: %s | Subject Code: %s\n", session.getClassroom(), session.getSubjectCode()));

        if (subject == null) {
            report.append("ℹ️ Non-academic / unrecorded course (e.g. Placement / Library). No attendance impact.\n");
        } else {
            double currentPct = subject.getAttendancePercentage();
            double ifBunkPct = subject.getProjectedPercentageIfBunk();
            double ifAttendPct = subject.getProjectedPercentageIfAttend();
            double decrease = currentPct - ifBunkPct;
            int safeBunks = subject.getSafeBunks();
            int needed = subject.getClassesNeededTo75();

            report.append(String.format("Current Attendance    : %d/%d (%.2f%%) [%s]\n",
                    subject.getAttendedClasses(), subject.getTotalClasses(), currentPct, subject.getStatus().getLabel()));
            report.append(String.format("If you ATTEND         : %d/%d -> %.2f%% (+%.2f%%)\n",
                    subject.getAttendedClasses() + 1, subject.getTotalClasses() + 1, ifAttendPct, ifAttendPct - currentPct));
            report.append(String.format("If you SKIP (BUNK)    : %d/%d -> %.2f%% (\u001B[31m-%.2f%% decrease\u001B[0m)\n",
                    subject.getAttendedClasses(), subject.getTotalClasses() + 1, ifBunkPct, decrease));

            if (ifBunkPct < 75.0) {
                if (currentPct >= 75.0) {
                    report.append(String.format("\u001B[31m✖ WILL IT DROP BELOW 75%%? YES! (Drops from %.2f%% to %.2f%%).\nVERDICT: DO NOT SKIP! This class puts you into the Danger Zone!\u001B[0m\n",
                            currentPct, ifBunkPct));
                } else {
                    report.append(String.format("\u001B[31m✖ WILL IT DROP BELOW 75%%? ALREADY IN DANGER (Drops from %.2f%% to %.2f%%).\nVERDICT: STRICT DANGER! You need %d more classes to reach 75%%. Do not miss this!\u001B[0m\n",
                            currentPct, ifBunkPct, needed));
                }
            } else {
                report.append(String.format("\u001B[32m✔ WILL IT DROP BELOW 75%%? NO! (Stays safe at %.2f%% >= 75.0%%).\nVERDICT: SAFE TO BUNK! (You can skip this period and still have %d safe bunks remaining).\u001B[0m\n",
                        ifBunkPct, Math.max(0, safeBunks - 1)));
            }
        }
        report.append(String.format("Remaining classes in queue: %d\n", scheduleQueue.size()));
        report.append("═══════════════════════════════════════════════════════════════");
        return report.toString();
    }
}


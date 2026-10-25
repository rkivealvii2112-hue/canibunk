package com.canibunk.service;

import com.canibunk.model.CalendarDay;
import com.canibunk.model.ClassSession;
import com.canibunk.model.SemesterProjection;
import com.canibunk.model.Subject;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Service that calculates semester-end attendance projections by combining
 * the student's current attendance records with the real Academic Calendar working days
 * and optional weekly lecture timetables.
 */
public class SemesterAttendanceProjector {

    /**
     * Determines how many lectures of the subject are remaining before the semester's
     * Last Instructional Day.
     *
     * If a weekly schedule is loaded in ScheduleManager, it evaluates each remaining
     * calendar working day according to its DayOfWeek and counts matching periods.
     * Otherwise, it calculates an accurate calendar-ratio projection based on elapsed
     * vs remaining working days.
     */
    public static int calculateRemainingClasses(Subject subject,
                                                LocalDate asOfDate,
                                                CalendarManager calendarManager,
                                                AcademicCalendarService calendarService,
                                                ScheduleManager scheduleManager) {
        if (calendarService == null || calendarManager == null) {
            return 10; // fallback conservative estimate
        }

        int remainingWorkingDays = calendarService.countWorkingDaysRemaining(calendarManager, asOfDate);
        if (remainingWorkingDays <= 0) {
            return 0;
        }

        // Check if scheduleManager has weekly schedule loaded
        if (scheduleManager != null && scheduleManager.getWeeklySchedule() != null && !scheduleManager.getWeeklySchedule().isEmpty()) {
            List<CalendarDay> remainingDays = calendarService.getRemainingInstructionalDays(calendarManager, asOfDate);
            int scheduledCount = 0;
            String code = subject.getSubjectCode().trim().toUpperCase();

            for (CalendarDay day : remainingDays) {
                DayOfWeek dow = day.getDate().getDayOfWeek();
                String dayName = dow.name(); // MONDAY, TUESDAY, etc.
                List<ClassSession> periods = scheduleManager.getSessionsForDay(dayName);
                if (periods != null) {
                    for (ClassSession session : periods) {
                        if (session.getSubjectCode() != null &&
                                session.getSubjectCode().trim().equalsIgnoreCase(code)) {
                            scheduledCount++;
                        }
                    }
                }
            }
            if (scheduledCount > 0) {
                return scheduledCount;
            }
        }

        // Proportional estimation based on calendar working days
        int elapsedWorkingDays = calendarService.countWorkingDaysElapsed(calendarManager, asOfDate);
        if (elapsedWorkingDays <= 0) elapsedWorkingDays = 1;

        int conducted = subject.getTotalClasses();
        if (conducted <= 0) {
            return Math.max(1, remainingWorkingDays);
        }

        int estimated = (int) Math.round(((double) conducted * remainingWorkingDays) / elapsedWorkingDays);
        return Math.max(1, estimated);
    }

    /**
     * Computes the complete SemesterProjection for a subject.
     */
    public static SemesterProjection project(Subject subject,
                                             LocalDate asOfDate,
                                             CalendarManager calendarManager,
                                             AcademicCalendarService calendarService,
                                             ScheduleManager scheduleManager) {
        int remainingClasses = calculateRemainingClasses(subject, asOfDate, calendarManager, calendarService, scheduleManager);
        int conducted = subject.getTotalClasses();
        int attended = subject.getAttendedClasses();
        int projectedTotal = conducted + remainingClasses;

        if (projectedTotal <= 0) {
            return new SemesterProjection(subject, 0, 0, 100.0, true, 0, 0, "No classes conducted or projected.");
        }

        double maxPossiblePct = AttendanceCalculator.calculateMaxPossiblePercentage(attended, conducted, remainingClasses);
        boolean canReach75 = AttendanceCalculator.canReachThreshold(attended, conducted, remainingClasses);
        int targetAttended = (int) Math.ceil(0.75 * projectedTotal);
        int mustAttend = AttendanceCalculator.calculateSemesterMustAttend(attended, conducted, remainingClasses);
        int semesterBunkBudget = AttendanceCalculator.calculateSemesterBunkBudget(attended, conducted, remainingClasses);

        String verdict;
        if (!canReach75) {
            int shortage = targetAttended - (attended + remainingClasses);
            verdict = String.format("CRITICAL SHORTAGE: Cannot reach 75%%! Short by %d class(es). Apply for Condonation before %s!",
                    shortage,
                    calendarService != null ? calendarService.getCondonationDeadlineNoFine() : "deadline");
        } else if (mustAttend > remainingClasses) {
            verdict = "CRITICAL: Cannot reach 75% with remaining classes!";
        } else if (mustAttend == remainingClasses && remainingClasses > 0) {
            verdict = String.format("BORDERLINE RECOVERY: Must attend ALL %d remaining classes! (0 bunks left)",
                    remainingClasses);
        } else if (mustAttend > 0) {
            verdict = String.format("RECOVERY: Must attend at least %d of %d remaining classes. (%d bunks permitted)",
                    mustAttend, remainingClasses, semesterBunkBudget);
        } else {
            verdict = String.format("SAFE: 75%% secured! You have a budget of %d safe bunk(s) for the rest of the semester.",
                    semesterBunkBudget);
        }

        return new SemesterProjection(
                subject,
                remainingClasses,
                projectedTotal,
                maxPossiblePct,
                canReach75,
                mustAttend,
                semesterBunkBudget,
                verdict
        );
    }
}

package com.canibunk.model;

/**
 * Encapsulates semester-end attendance projections based on the official academic calendar.
 * Integrates the exact count of remaining working days to calculate realistic future classes,
 * maximum achievable attendance, required classes to attend, and total remaining bunk budget.
 */
public class SemesterProjection {
    private final Subject subject;
    private final int remainingClasses;
    private final int projectedTotalClasses;
    private final double maxPossiblePercentage;
    private final boolean canAchieveThreshold;
    private final int mustAttendToPass;
    private final int semesterBunksRemaining;
    private final String verdict;

    public SemesterProjection(Subject subject,
                              int remainingClasses,
                              int projectedTotalClasses,
                              double maxPossiblePercentage,
                              boolean canAchieveThreshold,
                              int mustAttendToPass,
                              int semesterBunksRemaining,
                              String verdict) {
        this.subject = subject;
        this.remainingClasses = remainingClasses;
        this.projectedTotalClasses = projectedTotalClasses;
        this.maxPossiblePercentage = maxPossiblePercentage;
        this.canAchieveThreshold = canAchieveThreshold;
        this.mustAttendToPass = mustAttendToPass;
        this.semesterBunksRemaining = semesterBunksRemaining;
        this.verdict = verdict;
    }

    public Subject getSubject() {
        return subject;
    }

    public int getRemainingClasses() {
        return remainingClasses;
    }

    public int getProjectedTotalClasses() {
        return projectedTotalClasses;
    }

    public double getMaxPossiblePercentage() {
        return maxPossiblePercentage;
    }

    public boolean isCanAchieveThreshold() {
        return canAchieveThreshold;
    }

    public int getMustAttendToPass() {
        return mustAttendToPass;
    }

    public int getSemesterBunksRemaining() {
        return semesterBunksRemaining;
    }

    public String getVerdict() {
        return verdict;
    }

    @Override
    public String toString() {
        return String.format("[%s] Classes Left: %d | Projected Total: %d | Max Possible: %.2f%% | Must Attend: %d | Bunk Budget: %d | %s",
                subject.getSubjectCode(),
                remainingClasses,
                projectedTotalClasses,
                maxPossiblePercentage,
                mustAttendToPass,
                semesterBunksRemaining,
                verdict);
    }
}

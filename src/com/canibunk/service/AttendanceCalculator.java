package com.canibunk.service;

/**
 * Core mathematical engine for attendance calculation and bunking predictions.
 * Standard college threshold is 75.0% (strict).
 */
public class AttendanceCalculator {

    public static final double DEFAULT_THRESHOLD = 75.0; // 75% requirement

    /**
     * Calculates the current attendance percentage.
     * Percentage = (Attended / Conducted) * 100
     */
    public static double calculatePercentage(int attended, int conducted) {
        if (conducted <= 0) {
            return 100.0; // No classes conducted yet, default clean slate
        }
        return ((double) attended / conducted) * 100.0;
    }

    /**
     * Safe to Bunk Formula (If currently >= threshold):
     * Determines how many upcoming consecutive classes can be skipped before falling below 75%.
     * Formula:
     *   Attended / (Conducted + Bunks) >= 0.75
     *   Bunks <= (Attended / 0.75) - Conducted
     *   Safe Bunks = floor((Attended / 0.75) - Conducted)
     */
    public static int calculateSafeBunks(int attended, int conducted, double threshold) {
        if (conducted <= 0) {
            return 0;
        }
        double currentPct = calculatePercentage(attended, conducted);
        if (currentPct < threshold) {
            return 0; // In danger zone, no bunks allowed
        }
        double thresholdRatio = threshold / 100.0;
        double maxConductedAllowed = attended / thresholdRatio;
        int safeBunks = (int) Math.floor(maxConductedAllowed - conducted);
        return Math.max(0, safeBunks);
    }

    public static int calculateSafeBunks(int attended, int conducted) {
        return calculateSafeBunks(attended, conducted, DEFAULT_THRESHOLD);
    }

    /**
     * Danger Zone Formula (If currently < threshold):
     * Determines how many upcoming classes must be attended consecutively to reach 75%.
     * Formula:
     *   (Attended + Needed) / (Conducted + Needed) >= 0.75
     *   Attended + Needed >= 0.75 * Conducted + 0.75 * Needed
     *   (1 - 0.75) * Needed >= 0.75 * Conducted - Attended
     *   Needed >= (0.75 * Conducted - Attended) / 0.25
     *   Classes Needed = ceil((0.75 * Conducted - Attended) / 0.25)
     */
    public static int calculateClassesNeeded(int attended, int conducted, double threshold) {
        if (conducted <= 0) {
            return 0;
        }
        double currentPct = calculatePercentage(attended, conducted);
        if (currentPct >= threshold) {
            return 0; // Already safe
        }
        double thresholdRatio = threshold / 100.0;
        double numerator = (thresholdRatio * conducted) - attended;
        double denominator = 1.0 - thresholdRatio;
        int needed = (int) Math.ceil(numerator / denominator);
        return Math.max(0, needed);
    }

    public static int calculateClassesNeeded(int attended, int conducted) {
        return calculateClassesNeeded(attended, conducted, DEFAULT_THRESHOLD);
    }

    /**
     * Evaluates if the next single hour is bunkable.
     * True if and only if bunking 1 class keeps attendance >= threshold.
     */
    public static boolean isBunkableNextHour(int attended, int conducted, double threshold) {
        if (conducted <= 0) {
            return false;
        }
        double projectedIfBunk = ((double) attended / (conducted + 1)) * 100.0;
        return projectedIfBunk >= threshold;
    }

    public static boolean isBunkableNextHour(int attended, int conducted) {
        return isBunkableNextHour(attended, conducted, DEFAULT_THRESHOLD);
    }

    /**
     * Projected percentage if student skips the next hour.
     */
    public static double projectedPercentageIfBunk(int attended, int conducted) {
        return ((double) attended / (conducted + 1)) * 100.0;
    }

    /**
     * Projected percentage if student attends the next hour.
     */
    public static double projectedPercentageIfAttend(int attended, int conducted) {
        return ((double) (attended + 1) / (conducted + 1)) * 100.0;
    }

    /**
     * Maximum possible percentage achievable by semester end assuming 100% attendance
     * across all remaining classes.
     *
     * Formula: ((Attended + Remaining) / (Conducted + Remaining)) * 100
     */
    public static double calculateMaxPossiblePercentage(int attended, int conducted, int remainingClasses) {
        int projectedTotal = conducted + remainingClasses;
        if (projectedTotal <= 0) return 100.0;
        return ((double) (attended + remainingClasses) / projectedTotal) * 100.0;
    }

    /**
     * Evaluates if reaching the threshold is mathematically possible before semester end.
     */
    public static boolean canReachThreshold(int attended, int conducted, int remainingClasses, double threshold) {
        return calculateMaxPossiblePercentage(attended, conducted, remainingClasses) >= threshold;
    }

    public static boolean canReachThreshold(int attended, int conducted, int remainingClasses) {
        return canReachThreshold(attended, conducted, remainingClasses, DEFAULT_THRESHOLD);
    }

    /**
     * Calculates the minimum number of remaining classes that must be attended
     * to finish the semester with attendance >= threshold.
     *
     * Formula: ceil(threshold * (Conducted + Remaining)) - Attended
     */
    public static int calculateSemesterMustAttend(int attended, int conducted, int remainingClasses, double threshold) {
        int projectedTotal = conducted + remainingClasses;
        if (projectedTotal <= 0) return 0;
        double thresholdRatio = threshold / 100.0;
        int targetAttended = (int) Math.ceil(thresholdRatio * projectedTotal);
        return Math.max(0, targetAttended - attended);
    }

    public static int calculateSemesterMustAttend(int attended, int conducted, int remainingClasses) {
        return calculateSemesterMustAttend(attended, conducted, remainingClasses, DEFAULT_THRESHOLD);
    }

    /**
     * Calculates the remaining semester bunk budget (maximum absences permitted
     * across the remainder of the semester while keeping final attendance >= threshold).
     *
     * Formula: floor((1 - threshold) * (Conducted + Remaining)) - (Conducted - Attended)
     */
    public static int calculateSemesterBunkBudget(int attended, int conducted, int remainingClasses, double threshold) {
        int projectedTotal = conducted + remainingClasses;
        if (projectedTotal <= 0) return 0;
        double maxAbsenceRatio = (100.0 - threshold) / 100.0;
        int maxAllowedSemesterAbsences = (int) Math.floor(maxAbsenceRatio * projectedTotal);
        int absencesTaken = conducted - attended;
        return Math.max(0, maxAllowedSemesterAbsences - absencesTaken);
    }

    public static int calculateSemesterBunkBudget(int attended, int conducted, int remainingClasses) {
        return calculateSemesterBunkBudget(attended, conducted, remainingClasses, DEFAULT_THRESHOLD);
    }
}

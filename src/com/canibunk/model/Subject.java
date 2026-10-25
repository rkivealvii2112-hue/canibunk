package com.canibunk.model;

import com.canibunk.service.AttendanceCalculator;

/**
 * Represents an academic course subject with attendance statistics and bunk predictions.
 */
public class Subject implements Comparable<Subject> {
    private final String subjectCode;
    private final String subjectName;
    private int attendedClasses;
    private int totalClasses;
    private int credits;

    public Subject(String subjectCode, String subjectName, int attendedClasses, int totalClasses, int credits) {
        if (attendedClasses < 0 || totalClasses < 0 || attendedClasses > totalClasses) {
            throw new IllegalArgumentException("Invalid attendance figures: attended=" 
                    + attendedClasses + ", total=" + totalClasses);
        }
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.attendedClasses = attendedClasses;
        this.totalClasses = totalClasses;
        this.credits = credits;
    }

    public Subject(String subjectCode, String subjectName, int attendedClasses, int totalClasses) {
        this(subjectCode, subjectName, attendedClasses, totalClasses, 3);
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getAttendedClasses() {
        return attendedClasses;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public double getAttendancePercentage() {
        return AttendanceCalculator.calculatePercentage(attendedClasses, totalClasses);
    }

    public int getSafeBunks() {
        return AttendanceCalculator.calculateSafeBunks(attendedClasses, totalClasses);
    }

    public int getClassesNeededTo75() {
        return AttendanceCalculator.calculateClassesNeeded(attendedClasses, totalClasses);
    }

    public boolean isBunkableNextHour() {
        return AttendanceCalculator.isBunkableNextHour(attendedClasses, totalClasses);
    }

    public double getProjectedPercentageIfBunk() {
        return AttendanceCalculator.projectedPercentageIfBunk(attendedClasses, totalClasses);
    }

    public double getProjectedPercentageIfAttend() {
        return AttendanceCalculator.projectedPercentageIfAttend(attendedClasses, totalClasses);
    }

    public AttendanceStatus getStatus() {
        double pct = getAttendancePercentage();
        if (pct < AttendanceCalculator.DEFAULT_THRESHOLD) {
            return AttendanceStatus.CRITICAL;
        } else if (pct < 80.0 || getSafeBunks() == 0) {
            return AttendanceStatus.ON_THE_EDGE;
        } else {
            return AttendanceStatus.SAFE;
        }
    }

    // --- State mutation methods for the "What-If" Simulator ---

    public void attendClass() {
        this.attendedClasses++;
        this.totalClasses++;
    }

    public void bunkClass() {
        this.totalClasses++;
    }

    public void revertAttend() {
        if (this.attendedClasses > 0 && this.totalClasses > 0) {
            this.attendedClasses--;
            this.totalClasses--;
        }
    }

    public void revertBunk() {
        if (this.totalClasses > this.attendedClasses) {
            this.totalClasses--;
        }
    }

    /**
     * Creates a deep snapshot copy of the subject.
     */
    public Subject copy() {
        return new Subject(this.subjectCode, this.subjectName, this.attendedClasses, this.totalClasses, this.credits);
    }

    @Override
    public int compareTo(Subject other) {
        // Natural comparison based on attendance percentage (ascending for BST Danger Sorting)
        int cmp = Double.compare(this.getAttendancePercentage(), other.getAttendancePercentage());
        if (cmp != 0) {
            return cmp;
        }
        return this.subjectCode.compareTo(other.subjectCode);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %d/%d (%.2f%%) | Bunks Left: %d | Needed: %d | Next Hour: %s",
                subjectCode,
                subjectName,
                attendedClasses,
                totalClasses,
                getAttendancePercentage(),
                getSafeBunks(),
                getClassesNeededTo75(),
                isBunkableNextHour() ? "SAFE TO BUNK" : "CANNOT BUNK"
        );
    }
}

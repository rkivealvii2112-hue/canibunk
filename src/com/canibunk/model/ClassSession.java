package com.canibunk.model;

/**
 * Represents a single scheduled class period in a day's timetable.
 * Enqueued into the Daily Schedule Queue.
 */
public class ClassSession {
    private final int periodNumber;
    private final String timeSlot;
    private final String subjectCode;
    private final String subjectName;
    private final String classroom;

    public ClassSession(int periodNumber, String timeSlot, String subjectCode, String subjectName, String classroom) {
        this.periodNumber = periodNumber;
        this.timeSlot = timeSlot;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.classroom = classroom;
    }

    public int getPeriodNumber() {
        return periodNumber;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getClassroom() {
        return classroom;
    }

    @Override
    public String toString() {
        return String.format("Period %d (%s) -> [%s] %s @ Room %s",
                periodNumber, timeSlot, subjectCode, subjectName, classroom);
    }
}

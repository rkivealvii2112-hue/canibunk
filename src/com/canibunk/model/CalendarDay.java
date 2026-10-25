package com.canibunk.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single day in the academic calendar semester.
 * Used as node payload in the Doubly Linked List.
 */
public class CalendarDay {
    private final LocalDate date;
    private final boolean workingDay;
    private final String description;
    private final int dayNumberInSemester;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)");

    public CalendarDay(LocalDate date, boolean workingDay, String description, int dayNumberInSemester) {
        this.date = date;
        this.workingDay = workingDay;
        this.description = description;
        this.dayNumberInSemester = dayNumberInSemester;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isWorkingDay() {
        return workingDay;
    }

    public String getDescription() {
        return description;
    }

    public int getDayNumberInSemester() {
        return dayNumberInSemester;
    }

    @Override
    public String toString() {
        return String.format("Day #%02d: %s | %s [%s]",
                dayNumberInSemester,
                date.format(FORMATTER),
                workingDay ? "WORKING DAY" : "HOLIDAY/BREAK",
                description
        );
    }
}

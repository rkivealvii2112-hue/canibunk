package com.canibunk.model;

/**
 * Attendance status classification for a subject based on 75% threshold.
 */
public enum AttendanceStatus {
    CRITICAL("Danger Zone (< 75%)", "\u001B[31m"),       // Red
    ON_THE_EDGE("On The Edge (75% - 80%)", "\u001B[33m"), // Yellow
    SAFE("Safe Zone (> 80%)", "\u001B[32m");              // Green

    private final String label;
    private final String ansiColor;

    AttendanceStatus(String label, String ansiColor) {
        this.label = label;
        this.ansiColor = ansiColor;
    }

    public String getLabel() {
        return label;
    }

    public String getAnsiColor() {
        return ansiColor;
    }

    public String format(String text) {
        return ansiColor + text + "\u001B[0m";
    }
}

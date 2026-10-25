package com.canibunk.model;

/**
 * Encapsulates an action in the "What-If" attendance simulator.
 * Pushed onto the CustomStack to enable multi-step Undo/Redo operations.
 */
public class SimulationAction {

    public enum Type {
        ATTEND,
        BUNK
    }

    private final Subject subject;
    private final Type type;
    private final int prevAttended;
    private final int prevTotal;
    private final String description;

    public SimulationAction(Subject subject, Type type, String description) {
        this.subject = subject;
        this.type = type;
        this.prevAttended = subject.getAttendedClasses();
        this.prevTotal = subject.getTotalClasses();
        this.description = description;
    }

    public Subject getSubject() {
        return subject;
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Executes the simulation mutation.
     */
    public void execute() {
        if (type == Type.ATTEND) {
            subject.attendClass();
        } else {
            subject.bunkClass();
        }
    }

    /**
     * Undoes the simulation action, restoring the subject's exact previous state.
     */
    public void undo() {
        if (type == Type.ATTEND) {
            subject.revertAttend();
        } else {
            subject.revertBunk();
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] on %s (%s) | Prev: %d/%d -> Now: %d/%d (%.2f%%)",
                type,
                subject.getSubjectCode(),
                description != null ? description : "",
                prevAttended,
                prevTotal,
                subject.getAttendedClasses(),
                subject.getTotalClasses(),
                subject.getAttendancePercentage());
    }
}

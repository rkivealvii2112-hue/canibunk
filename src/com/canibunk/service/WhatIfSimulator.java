package com.canibunk.service;

import com.canibunk.ds.CustomStack;
import com.canibunk.model.SimulationAction;
import com.canibunk.model.Subject;

/**
 * Manages the "What-If" attendance simulation feature using CustomStack (LIFO).
 * Allows students to simulate attending or bunking upcoming classes and
 * freely undo or redo decisions to evaluate different scenarios.
 */
public class WhatIfSimulator {
    private final CustomStack<SimulationAction> undoStack;
    private final CustomStack<SimulationAction> redoStack;

    public WhatIfSimulator() {
        this.undoStack = new CustomStack<>();
        this.redoStack = new CustomStack<>();
    }

    /**
     * Simulates bunking a class for the given subject.
     */
    public SimulationAction simulateBunk(Subject subject, String note) {
        SimulationAction action = new SimulationAction(subject, SimulationAction.Type.BUNK, note);
        action.execute();
        undoStack.push(action);
        redoStack.clear(); // new action invalidates redo history
        return action;
    }

    /**
     * Simulates attending a class for the given subject.
     */
    public SimulationAction simulateAttend(Subject subject, String note) {
        SimulationAction action = new SimulationAction(subject, SimulationAction.Type.ATTEND, note);
        action.execute();
        undoStack.push(action);
        redoStack.clear();
        return action;
    }

    /**
     * Undoes the last simulation action by popping from undoStack.
     * Time Complexity: O(1).
     */
    public SimulationAction undo() {
        if (undoStack.isEmpty()) {
            return null;
        }
        SimulationAction action = undoStack.pop();
        action.undo();
        redoStack.push(action);
        return action;
    }

    /**
     * Redoes the last undone simulation action.
     * Time Complexity: O(1).
     */
    public SimulationAction redo() {
        if (redoStack.isEmpty()) {
            return null;
        }
        SimulationAction action = redoStack.pop();
        action.execute();
        undoStack.push(action);
        return action;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public int getUndoCount() {
        return undoStack.size();
    }

    public int getRedoCount() {
        return redoStack.size();
    }
}

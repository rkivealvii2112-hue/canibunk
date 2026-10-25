package com.canibunk.service;

import com.canibunk.ds.DoublyLinkedList;
import com.canibunk.ds.DoublyLinkedList.Node;
import com.canibunk.model.CalendarDay;
import java.time.LocalDate;

/**
 * Manages the Academic Calendar using a Doubly Linked List (bidirectional traversal).
 * Enables forward traversal to count remaining working days till semester end,
 * and backward traversal from exam dates to evaluate study breaks and prep buffers.
 */
public class CalendarManager {
    private final DoublyLinkedList<CalendarDay> calendarList;

    public CalendarManager() {
        this.calendarList = new DoublyLinkedList<>();
    }

    public void addDay(CalendarDay day) {
        calendarList.addLast(day);
    }

    public DoublyLinkedList<CalendarDay> getCalendarList() {
        return calendarList;
    }

    public Node<CalendarDay> findDayNode(LocalDate date) {
        return calendarList.findNode(day -> day.getDate().equals(date));
    }

    /**
     * Traverses forward from the given date to the end of the semester,
     * counting the total number of working days remaining.
     */
    public int countWorkingDaysRemaining(LocalDate fromDate) {
        Node<CalendarDay> curr = findDayNode(fromDate);
        if (curr == null) {
            // If fromDate is not in list, find first date >= fromDate
            curr = calendarList.findNode(day -> !day.getDate().isBefore(fromDate));
        }

        int workingDays = 0;
        while (curr != null) {
            if (curr.getData().isWorkingDay()) {
                workingDays++;
            }
            curr = curr.getNext();
        }
        return workingDays;
    }

    /**
     * Traverses forward from fromDate up to lastInstructionalDay (inclusive),
     * counting only instructional working days where regular classes are conducted.
     */
    public int countInstructionalWorkingDaysRemaining(LocalDate fromDate, LocalDate lastInstructionalDay) {
        if (lastInstructionalDay != null && fromDate.isAfter(lastInstructionalDay)) {
            return 0;
        }
        Node<CalendarDay> curr = findDayNode(fromDate);
        if (curr == null) {
            curr = calendarList.findNode(day -> !day.getDate().isBefore(fromDate));
        }

        int workingDays = 0;
        while (curr != null) {
            if (lastInstructionalDay != null && curr.getData().getDate().isAfter(lastInstructionalDay)) {
                break;
            }
            if (curr.getData().isWorkingDay()) {
                workingDays++;
            }
            curr = curr.getNext();
        }
        return workingDays;
    }

    /**
     * Traverses backward from an exam / milestone date to count days between now and then.
     */
    public int countDaysUntilExam(LocalDate examDate, LocalDate fromDate) {
        Node<CalendarDay> examNode = findDayNode(examDate);
        if (examNode == null) return -1;

        int days = 0;
        Node<CalendarDay> curr = examNode;
        while (curr != null) {
            if (curr.getData().getDate().equals(fromDate)) {
                return days;
            }
            days++;
            curr = curr.getPrev();
        }
        return -1; // fromDate not found before examDate
    }

    public int getTotalDays() {
        return calendarList.size();
    }
}

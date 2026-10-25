package com.canibunk.ds;

import java.util.NoSuchElementException;

/**
 * Custom Generic Circular Queue implementation (FIFO - First In First Out) using an array.
 * Demonstrates circular buffer modulo arithmetic for O(1) enqueue and dequeue operations.
 * Used in CanIBunk for the Daily Schedule (loading timetable, advancing periods).
 *
 * @param <T> element type
 */
public class CustomQueue<T> {
    private static final int DEFAULT_CAPACITY = 8;
    private Object[] elements;
    private int front;
    private int rear;
    private int count;

    public CustomQueue() {
        this(DEFAULT_CAPACITY);
    }

    public CustomQueue(int capacity) {
        if (capacity <= 0) {
            capacity = DEFAULT_CAPACITY;
        }
        this.elements = new Object[capacity];
        this.front = 0;
        this.rear = 0;
        this.count = 0;
    }

    /**
     * Inserts an element at the rear of the queue.
     * Time Complexity: O(1) amortized.
     */
    public void enqueue(T item) {
        if (count == elements.length) {
            resize(elements.length * 2);
        }
        elements[rear] = item;
        rear = (rear + 1) % elements.length;
        count++;
    }

    /**
     * Removes and returns the element at the front of the queue.
     * Time Complexity: O(1).
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        T item = (T) elements[front];
        elements[front] = null; // free reference
        front = (front + 1) % elements.length;
        count--;
        return item;
    }

    /**
     * Retrieves, but does not remove, the element at the front of the queue.
     * Time Complexity: O(1).
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return (T) elements[front];
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public int size() {
        return count;
    }

    public void clear() {
        int idx = front;
        for (int i = 0; i < count; i++) {
            elements[idx] = null;
            idx = (idx + 1) % elements.length;
        }
        front = 0;
        rear = 0;
        count = 0;
    }

    private void resize(int newCapacity) {
        Object[] newElements = new Object[newCapacity];
        for (int i = 0; i < count; i++) {
            newElements[i] = elements[(front + i) % elements.length];
        }
        elements = newElements;
        front = 0;
        rear = count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CustomQueue[");
        for (int i = 0; i < count; i++) {
            sb.append(elements[(front + i) % elements.length]);
            if (i < count - 1) {
                sb.append(" -> ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

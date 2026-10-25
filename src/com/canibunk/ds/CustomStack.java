package com.canibunk.ds;

import java.util.EmptyStackException;

/**
 * Custom Generic Stack implementation (LIFO - Last In First Out) using a dynamic array.
 * Used in CanIBunk for the "What-If" Attendance Simulator (pushing actions, undo/redo).
 *
 * @param <T> element type
 */
public class CustomStack<T> {
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elements;
    private int top; // index of next available position (also size)

    public CustomStack() {
        this(DEFAULT_CAPACITY);
    }

    public CustomStack(int initialCapacity) {
        if (initialCapacity <= 0) {
            initialCapacity = DEFAULT_CAPACITY;
        }
        this.elements = new Object[initialCapacity];
        this.top = 0;
    }

    /**
     * Pushes an item onto the top of the stack.
     * Time Complexity: O(1) amortized.
     */
    public void push(T item) {
        if (top == elements.length) {
            resize(elements.length * 2);
        }
        elements[top++] = item;
    }

    /**
     * Removes and returns the item at the top of the stack.
     * Time Complexity: O(1).
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T item = (T) elements[--top];
        elements[top] = null; // avoid memory leak
        return item;
    }

    /**
     * Returns the item at the top of the stack without removing it.
     * Time Complexity: O(1).
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return (T) elements[top - 1];
    }

    public boolean isEmpty() {
        return top == 0;
    }

    public int size() {
        return top;
    }

    public void clear() {
        for (int i = 0; i < top; i++) {
            elements[i] = null;
        }
        top = 0;
    }

    private void resize(int newCapacity) {
        Object[] newElements = new Object[newCapacity];
        System.arraycopy(elements, 0, newElements, 0, top);
        elements = newElements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CustomStack[");
        for (int i = 0; i < top; i++) {
            sb.append(elements[i]);
            if (i < top - 1) {
                sb.append(", ");
            }
        }
        sb.append("] (Top: ").append(top > 0 ? elements[top - 1] : "empty").append(")");
        return sb.toString();
    }
}

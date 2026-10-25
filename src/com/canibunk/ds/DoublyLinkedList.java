package com.canibunk.ds;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Custom Doubly Linked List implementation.
 * Each node maintains bidirectional pointers (prev and next).
 * Used in CanIBunk for the Academic Calendar to navigate forward and backward
 * across semester days and compute working days left.
 *
 * @param <T> element type
 */
public class DoublyLinkedList<T> implements Iterable<T> {

    public static class Node<T> {
        private T data;
        private Node<T> prev;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Node<T> getPrev() {
            return prev;
        }

        public Node<T> getNext() {
            return next;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public DoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Appends an element to the tail of the doubly linked list.
     * Time Complexity: O(1).
     */
    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    /**
     * Prepends an element to the head of the doubly linked list.
     * Time Complexity: O(1).
     */
    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        size++;
    }

    public Node<T> getHead() {
        return head;
    }

    public Node<T> getTail() {
        return tail;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Finds the first node satisfying the given condition.
     */
    public Node<T> findNode(Predicate<T> predicate) {
        Node<T> curr = head;
        while (curr != null) {
            if (predicate.test(curr.data)) {
                return curr;
            }
            curr = curr.next;
        }
        return null;
    }

    /**
     * Traverses forward from head to tail.
     */
    public void traverseForward(Consumer<T> consumer) {
        Node<T> curr = head;
        while (curr != null) {
            consumer.accept(curr.data);
            curr = curr.next;
        }
    }

    /**
     * Traverses backward from tail to head.
     */
    public void traverseBackward(Consumer<T> consumer) {
        Node<T> curr = tail;
        while (curr != null) {
            consumer.accept(curr.data);
            curr = curr.prev;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }
}

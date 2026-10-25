package com.canibunk.ds;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Hash Map implementation using an Array of Buckets and Separate Chaining
 * (singly linked list collision resolution).
 * Demonstrates O(1) average lookup, insertion, and dynamic rehashing.
 * Used in CanIBunk to store Subject records indexed by Subject Code (e.g. "CS301").
 *
 * @param <K> key type
 * @param <V> value type
 */
public class CustomHashMap<K, V> {

    public static class Entry<K, V> {
        private final K key;
        private V value;
        private Entry<K, V> next;

        public Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] buckets = new Entry[DEFAULT_CAPACITY];
    private int size = 0;

    public CustomHashMap() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public CustomHashMap(int capacity) {
        if (capacity <= 0) capacity = DEFAULT_CAPACITY;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    private int getBucketIndex(K key) {
        if (key == null) return 0;
        int hash = key.hashCode();
        return Math.abs(hash % buckets.length);
    }

    /**
     * Associates the specified value with the specified key.
     * Time Complexity: O(1) average.
     */
    public void put(K key, V value) {
        int index = getBucketIndex(key);
        Entry<K, V> head = buckets[index];

        Entry<K, V> curr = head;
        while (curr != null) {
            if ((key == null && curr.key == null) || (key != null && key.equals(curr.key))) {
                curr.value = value; // update existing value
                return;
            }
            curr = curr.next;
        }

        // Key not found, insert at head of chain
        Entry<K, V> newEntry = new Entry<>(key, value, head);
        buckets[index] = newEntry;
        size++;

        if ((float) size / buckets.length >= LOAD_FACTOR) {
            rehash();
        }
    }

    /**
     * Returns the value mapped to key, or null if not found.
     * Time Complexity: O(1) average.
     */
    public V get(K key) {
        int index = getBucketIndex(key);
        Entry<K, V> curr = buckets[index];
        while (curr != null) {
            if ((key == null && curr.key == null) || (key != null && key.equals(curr.key))) {
                return curr.value;
            }
            curr = curr.next;
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /**
     * Removes mapping for key.
     * Time Complexity: O(1) average.
     */
    public V remove(K key) {
        int index = getBucketIndex(key);
        Entry<K, V> curr = buckets[index];
        Entry<K, V> prev = null;

        while (curr != null) {
            if ((key == null && curr.key == null) || (key != null && key.equals(curr.key))) {
                if (prev == null) {
                    buckets[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return curr.value;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        this.buckets = new Entry[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public List<V> values() {
        List<V> list = new ArrayList<>();
        for (Entry<K, V> head : buckets) {
            Entry<K, V> curr = head;
            while (curr != null) {
                list.add(curr.value);
                curr = curr.next;
            }
        }
        return list;
    }

    public List<K> keySet() {
        List<K> list = new ArrayList<>();
        for (Entry<K, V> head : buckets) {
            Entry<K, V> curr = head;
            while (curr != null) {
                list.add(curr.key);
                curr = curr.next;
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        Entry<K, V>[] oldBuckets = buckets;
        buckets = new Entry[oldBuckets.length * 2];
        size = 0;

        for (Entry<K, V> head : oldBuckets) {
            Entry<K, V> curr = head;
            while (curr != null) {
                put(curr.key, curr.value);
                curr = curr.next;
            }
        }
    }
}

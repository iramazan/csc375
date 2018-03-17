package ConcurrentUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LinkedList {

    private static final class Node {
        final String key;
        volatile float value;
        static final VarHandle VALUE;
        AtomicMarkableReference<Node> next;
        static {
            try {
                VALUE = MethodHandles.lookup().
                        findVarHandle(Node.class, "value", float.class);
            }
            catch (ReflectiveOperationException e) {
                throw new Error(e);
            }
        }
        Node(String key, float value, Node next) {
            this.key = key;
            this.value = value;
            this.next = new AtomicMarkableReference<>(next, false);
        }
    }

    private volatile AtomicMarkableReference<Node> head;

    /**
     * Create a new ConcurrentUtil.LinkedList.
     */
    public LinkedList() {
        head = new AtomicMarkableReference<>(null, false);
    }

    /**
     * Get a set containing all the keys in this linked list.
     * @return The set containing all keys in this linked list
     */
    public Set<String> getKeys() {
        Set<String> keySet = new TreeSet<>();
        // set current to head
        Node curr = head.getReference();
        // for each node, add to set
        while (curr != null) {
            keySet.add(curr.key);
            curr = curr.next.getReference();
        }
        return keySet;
    }

    /**
     * Get a set containing the key value pairings in this linked list.
     * @return The set containing all key value pairings in this linked list
     */
    public List<Tuple<String, Float>> getEntries() {
        List<Tuple<String, Float>> entries = new ArrayList<>();
        Node curr = head.getReference();
        while (curr != null) {
            entries.add(new Tuple<>(curr.key, curr.value));
            curr = curr.next.getReference();
        }
        return entries;
    }

    /**
     * Get the value associated with parameter key from this Linked List.
     * @param key String key to search for
     * @return Optional containing the value or nothing if it is not found
     */
    public Optional<Float> getValue(String key) {
        // set current to head
        Node curr = head.getReference();
        // search for node matching key
        while (curr != null) {
            // match is found, return value
            if (curr.key.equals(key)) {
                return Optional.of((float)Node.VALUE.getAcquire(curr));
            }
            curr = curr.next.getReference();
        }
        // reached end of list, key is not in this
        return Optional.empty();
    }

    /**
     * Set a new value for a key.
     * @param key String key to search for
     * @param newValue New float value to update to
     */
    public void setValue(String key, float newValue) {
        // set current to head
        Node curr = head.getReference();
        // search for node matching key
        while (curr != null) {
            float currValue = (float) Node.VALUE.getAcquire(curr);
            // match is found, update value
            if (curr.key.equals(key)) {
                if (Node.VALUE.compareAndSet(curr, currValue, newValue)) {
                    return;
                }
            }
            curr = curr.next.getReference();
        }
    }

    /**
     * Modify the value of a key value pair already in this Linked List.
     * @param key String key of entry to modify
     * @param value Float value to add to current value
     * @return True if entry is found and modification made
     */
    public boolean addValue(String key, float value) {
        // set current to head
        Node curr = head.getReference();
        // search for node matching key
        while (curr != null) {
            // match is found, update value
            if (curr.key.equals(key)) {
                Node.VALUE.getAndAdd(curr, value);
                return true;
            }
            curr = curr.next.getReference();
        }
        // Key not found in linked list
        return false;
    }

    /**
     * Modify the value of all key value pairs in this Linked List.
     */
    public void modifyValues() {
        // set current to head
        Node curr = head.getReference();
        // modify value for each
        while (curr != null) {
            curr.value += ThreadLocalRandom.current().nextGaussian();
            curr = curr.next.getReference();
        }
    }

    /**
     * Prepend a key and value pair to the linked list.
     * @param key String key to add
     * @param value floating point value to add
     */
    public void add(String key, float value) {
        for (;;) {
            Node headRef = head.getReference();
            // Try CAS, if that succeeds exit method
            // if head is marked, loop should repeat until it isn't
            if (head.compareAndSet(headRef, new Node(key, value, headRef),
                    false, false)) {
                return;
            }
        }
    }

    /**
     * Get a random key value pairing from this list.
     * @return A key value pairing
     */
    public Tuple<String, Float> getRandom() {
        Node curr = head.getReference();
        while (curr != null) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                return new Tuple<>(curr.key, curr.value);
            }
            curr = curr.next.getReference();
        }
        // iterated through linked list without returning anything, just do head
        curr = head.getReference();
        if (curr != null) {
            return new Tuple<>(curr.key, curr.value);
        }
        else {
            return null;
        }
    }

    /**
     * Remove entry with String key from the Linked List.
     * @param key String key of removal candidate
     * @return True if removal is successful
     */
    public boolean remove(String key) {
        find:
        for (;;) {
            Node prev = null;
            Node curr = head.getReference();
            do {
                Node next = curr.next.getReference();
                // match is found
                if (curr.key.equals(key)) {
                    // if prev is null, head should be removed
                    if (prev == null) {
                        // mark reference succeeds, CAS Reference
                        if (curr.next.attemptMark(next, true)) {
                            // CAS succeeds
                            if (head.compareAndSet(curr, next, false, false)) {
                                return true;
                            }
                            // CAS fails, try again
                            else {
                                continue find;
                            }
                        }
                        // mark fails, go to beginning
                        else {
                            continue find;
                        }
                    }
                    else {
                        // mark reference succeeds, CAS reference
                        if (curr.next.attemptMark(next, true)) {
                            // CAS succeeds
                            if (prev.next.compareAndSet(curr, next, false, false)) {
                                return true;
                            }
                            // CAS Fails, try again
                            else {
                                continue find;
                            }
                        }
                        // mark fails, go to beginning
                        else {
                            continue find;
                        }
                    }
                }
                // iterate to next node
                prev = curr;
                curr = next;
            } while (curr != null);
            // not found, return false;
            return false;
        }
    }

}

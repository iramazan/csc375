package ConcurrentUtil;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HashTable {

    private LinkedList[] nodes;
    private int size;
    private int maxCapacity;
    private ReadWriteLock lock;

    public HashTable() {
        maxCapacity = 16;
        nodes = new LinkedList[maxCapacity];
        Arrays.setAll(nodes, e -> new LinkedList());
        size = 0;
        lock = new ReentrantReadWriteLock();
    }

    public int getSize() {
        return size;
    }

    private void addToArray(String key, float value, LinkedList[] nodeArray) throws InterruptedException {
        int hash = key.hashCode();
        // equivalent to hash % nodeArray.length for lengths equal to powers of 2
        int index = hash & (nodeArray.length - 1);
        Optional<Float> keyValue = nodeArray[index].getValue(key);
        // if key is not in ConcurrentUtil.LinkedList, add it
        if (!keyValue.isPresent()) {
            nodeArray[index].add(key, value);
            size++;
            // resize if size >= 75% capacity
            if (size >= 0.75*maxCapacity) {
                resize();
            }
        }
        // key is already in hashtable, add value
        else {
            nodeArray[index].addValue(key, value);
        }
    }

    // create new nodes array with double the size, re-add nodes
    private void resize() throws InterruptedException {
        lock.readLock().unlock();
        lock.writeLock().lock();
        try {
            maxCapacity *= 2;
            size = 0;
            LinkedList[] newNodes = new LinkedList[maxCapacity];
            Arrays.setAll(newNodes, e -> new LinkedList());
            for (LinkedList list : nodes) {
                list.getEntries().forEach((e) -> {
                    try {
                        addToArray(e.key, e.value, newNodes);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
            }
            nodes = newNodes;
        }
        finally {
            lock.writeLock().unlock();
            lock.readLock().lock();
        }
    }

    /**
     * Get the set of all keys in this hash table.
     * @return The set of all keys in this Hash Table
     */
    public Set<String> getKeys() throws InterruptedException {
        lock.readLock().lock();
        try {
            Set<String> keys = new TreeSet<>();
            for (LinkedList list : nodes) {
                keys.addAll(list.getKeys());
            }
            return keys;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get the value associated with String key
     * @param key String key to search for
     * @return Optional containing the value or nothing if it is not found
     */
    public float getValue(String key) throws InterruptedException {
        lock.readLock().lock();
        try {
            int hash = key.hashCode();
            // equivalent to hash % nodes.length for lengths equal to powers of 2
            int index = hash & (nodes.length - 1);
            Optional<Float> value = nodes[index].getValue(key);
            return value.orElse(0f);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Update the value associated with a String key
     * @param key String key to search for
     * @param newValue New value to update to
     * @throws InterruptedException Thread is interrupted
     */
    public void updatePair(String key, float newValue) throws InterruptedException {
        lock.readLock().lock();
        try {
            int hash = key.hashCode();
            int index = hash & (nodes.length - 1);
            nodes[index].setValue(key, newValue);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Modify the value of all stocks.
     * @throws InterruptedException Thread is interrupted
     */
    public void modifyValues() throws InterruptedException {
        lock.readLock().lock();
        try {
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].modifyValues();
            }
        }
        finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get a random key value pairing from this Hash Table.
     * @return A key value pair
     * @throws InterruptedException Thread is interrupted
     */
    public Tuple<String, Float> getRandom() throws InterruptedException {
        lock.readLock().lock();
        try {
            for (;;) {
                int i = ThreadLocalRandom.current().nextInt(nodes.length);
                Tuple<String, Float> tuple = nodes[i].getRandom();
                // make sure there was actually a value
                if (tuple != null) {
                    return tuple;
                }
            }
        }
        finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Add a key value pair to the Hash Table
     * @param key String key to add
     * @param value floating point value to add
     */
    public void add(String key, float value) throws InterruptedException {
        lock.readLock().lock();
        try {
            addToArray(key, value, this.nodes);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Remove entry with String key from the Hash Table
     * @param key String key to remove
     * @return True if removal is successful
     */
    public boolean remove(String key) throws InterruptedException {
        lock.readLock().lock();
        try {
            int hash = key.hashCode();
            // equivalent to hash % nodes.length for lengths equal to powers of 2
            int index = hash & (nodes.length - 1);
            boolean success = nodes[index].remove(key);
            if (success) {
                size--;
            }
            return success;
        }
        finally {
            lock.readLock().unlock();
        }
    }

}

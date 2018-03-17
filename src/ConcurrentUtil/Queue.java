package ConcurrentUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class Queue {

    private static final class Node {
        final Thread thread;
        Node next;
        final static VarHandle NEXT;
        static {
            try {
                NEXT = MethodHandles.lookup().
                        findVarHandle(Node.class, "next", Node.class);
            }
            catch (ReflectiveOperationException e) {
                throw new Error(e);
            }
        }
        Node(Thread thread, Node next) {
            this.thread = thread;
            this.next = next;
        }
    }

    private volatile Node head;
    private volatile Node tail;
    private final static VarHandle HEAD;
    private final static VarHandle TAIL;
    static {
        try {
            HEAD = MethodHandles.lookup().
                    findVarHandle(Queue.class, "head", Node.class);
            TAIL = MethodHandles.lookup().
                    findVarHandle(Queue.class, "tail", Node.class);
        }
        catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    /**
     * Create a new ConcurrentUtil.Queue object.
     */
    public Queue() {
        // Initialize with dummy node
        head = new Node(null, null);
        tail = head;
    }

    /**
     * Put a thread into the queue.
     * @param thread Thread object to put into the queue
     */
    public void put(Thread thread) {
        Node newNode = new Node(thread, null);
        for (;;) {
            Node tailRef = (Node) TAIL.getAcquire(this);
            Node next = (Node) Node.NEXT.getAcquire(tailRef);
            // check consistency
            if (tailRef == tail) {
                // check tail points to last node
                if (next == null) {
                    // Try to CAS new node to end of list
                    if (Node.NEXT.compareAndSet(tailRef, null, newNode)) {
                        break;
                    }
                }
                // tail isn't pointing to last node, swing it
                else {
                    TAIL.compareAndSet(this, tailRef, next);
                }
            }
        }
        // Update tail pointer to newly added node
        TAIL.compareAndSet(this, tail, tail.next);
    }

    /**
     * Poll a thread from the front of the queue.
     * @return Optional containing a thread object or nothing
     */
    public Optional<Thread> poll() {
        for (;;) {
            Node headRef = (Node) HEAD.getAcquire(this);
            Node tailRef = (Node) TAIL.getAcquire(this);
            Node next = (Node) Node.NEXT.getAcquire(head);
            // Check for consistency in values
            if (headRef == head) {
                // is queue empty, or tail "falling behind"
                if (headRef == tailRef) {
                    // ConcurrentUtil.Queue is empty, return nothing
                    if (next == null) {
                        return Optional.empty();
                    }
                    // Tail isn't pointing to last node, try to fix it
                    TAIL.compareAndSet(this, tailRef, tailRef.next);
                }
                // Just remove head
                else {
                    if (Node.NEXT.compareAndSet(head, next, next.next)) {
                        return Optional.of(next.thread);
                    }
                }
            }
        }
    }

    /**
     * Remove all threads from queue and return them in a set
     * @return Set containing all threads in this queue
     */
    public Set<Thread> pollAll() {
        Set<Thread> threads = new TreeSet<>();
        for (;;) {
            Optional<Thread> thread = poll();
            if (thread.isPresent()) {
                threads.add(thread.get());
            }
            else {
                return threads;
            }
        }
    }

}

package ConcurrentUtil;

import java.util.Optional;

public class Mutex {

    private boolean held;
    private Queue queue;

    /**
     * Creates a new ConcurrentUtil.Mutex object.
     */
    public Mutex() {
        held = false;
        queue = new Queue();
    }

    /**
     * Lock this lock.
     * @throws InterruptedException
     */
    public void lock() throws InterruptedException {
        Thread thread = Thread.currentThread();
        // if lock is held, add thread to queue and wait
        if (held) {
            queue.put(thread);
            thread.wait();
        }
        // give lock to this thread
        held = true;
    }

    /**
     * Unlock this lock.
     */
    public void unlock() {
        // poll queue for next thread
        Optional<Thread> thread = queue.poll();
        // if thread is present, notify it that it has the lock
        if (thread.isPresent()) {
            thread.get().notify();
        }
        // No more threads in queue, nobody holds lock
        else {
            held = false;
        }
    }

    /**
     * Return the status of this lock.
     * @return True if this lock is held by a thread
     */
    public boolean isHeld() {
        return held;
    }

}

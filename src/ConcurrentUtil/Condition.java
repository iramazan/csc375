package ConcurrentUtil;

import java.util.Optional;
import java.util.Set;

public class Condition {

    private Queue queue;

    public Condition() {
        queue = new Queue();
    }

    public void await(Mutex mutex) throws InterruptedException {
        Thread thread = Thread.currentThread();
        synchronized (thread) {
            mutex.unlock();
            queue.put(thread);
            thread.wait();
        }
        mutex.lock();
    }

    public void signal() {
        Optional<Thread> thread = queue.poll();
        if (!thread.isPresent()) {
            return;
        }
        synchronized (thread.get()) {
            thread.get().notify();
        }
    }

    public void signalAll() throws InterruptedException {
        Set<Thread> threads = queue.pollAll();
        for (Thread t : threads) {
            synchronized (t) {
                t.notify();
            }
        }
    }

}

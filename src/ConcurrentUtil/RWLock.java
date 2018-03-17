package ConcurrentUtil;

public class RWLock {

    private int readers;
    private int writers;
    private int waitingWriters;
    private final Mutex lock;
    private final Condition readCondition;
    private final Condition writeCondition;

    public RWLock() {
        lock = new Mutex();
        readCondition = new Condition();
        writeCondition = new Condition();
    }

    /**
     * Attempt to lock for a read.
     * @throws InterruptedException
     */
    public void lockRead() throws InterruptedException {
        lock.lock();
        try {
            for (;;) {
                if (writers == 0 && waitingWriters == 0) {
                    readers++;
                    break;
                }
                readCondition.await(lock);
            }
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Unlock a reader.
     * @throws InterruptedException
     */
    public void unlockRead() throws InterruptedException {
        lock.lock();
        try {
            if (--readers == 0) {
                if (waitingWriters > 0) {
                    writeCondition.signal();
                }
                else {
                    readCondition.signalAll();
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Attempt to lock for a write.
     * @throws InterruptedException
     */
    public void lockWrite() throws InterruptedException {
        lock.lock();
        try {
            for (;;) {
                if (readers == 0 && writers == 0) {
                    writers++;
                    break;
                }
                waitingWriters++;
                writeCondition.await(lock);
                waitingWriters--;
            }
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Unlock a writer.
     * @throws InterruptedException
     */
    public void unlockWrite() throws InterruptedException {
        lock.lock();
        try {
            if (--writers == 0) {
                if (waitingWriters > 0) {
                    writeCondition.signal();
                }
                else {
                    readCondition.signalAll();
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Upgrade a reader to a writer.
     * @throws InterruptedException
     */
    public void upgradeWrite() throws InterruptedException {
        lock.lock();
        try {
            for (;;) {
                if (--readers == 0 && writers == 0) {
                    writers++;
                    break;
                }
                waitingWriters++;
                writeCondition.await(lock);
                waitingWriters--;
            }
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Downgrade a writer to a reader.
     * @throws InterruptedException
     */
    public void downgradeWrite() throws InterruptedException {
        lock.lock();
        try {
            writers--;
            readers++;
            readCondition.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

}

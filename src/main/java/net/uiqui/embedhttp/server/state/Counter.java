package net.uiqui.embedhttp.server.state;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {
    private final Lock lock = new ReentrantLock();
    private final Condition isZero  = lock.newCondition();
    private int value = 0;

    public void addOne() {
        lock.lock();

        try {
            value++;
        } finally {
            lock.unlock();
        }
    }

    public void downOne() {
        lock.lock();

        try {
            value--;

            if (value == 0) {
                isZero.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void await() throws InterruptedException {
        lock.lock();
        try {
            while (value > 0) {
                isZero.await();
            }
        } finally {
            lock.unlock();
        }
    }
}

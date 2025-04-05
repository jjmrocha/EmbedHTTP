package net.uiqui.embedhttp.status;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StatusHolder {
    private static final Map<Status, Set<Status>> VALID_STATUS_CHANGE = Map.of(
            Status.STARTING, Set.of(Status.RUNNING, Status.STOPPED),
            Status.RUNNING, Set.of(Status.STOPPING, Status.STOPPED),
            Status.STOPPING, Set.of(Status.STOPPED),
            Status.STOPPED, Set.of(Status.STARTING)
    );

    private final Lock lock = new ReentrantLock();
    private final Condition statusChanged = lock.newCondition();

    private Status currentStatus;

    public StatusHolder(Status initialStatus) {
        this.currentStatus = initialStatus;
    }

    public Status getCurrentStatus() {
        lock.lock();
        try {
            return currentStatus;
        } finally {
            lock.unlock();
        }
    }

    public Status waitForStatus(Status... status) throws InterruptedException {
        var wantedStatus = Set.of(status);

        lock.lock();
        try {
            while (!wantedStatus.contains(currentStatus)) {
                statusChanged.await();
            }

            return currentStatus;
        } finally {
            lock.unlock();
        }
    }

    public boolean setStatus(Status newStatus) {
        lock.lock();
        try {
            if (!isValidStatusChange(currentStatus, newStatus)) {
                return false;
            }

            currentStatus = newStatus;
            statusChanged.signalAll();

            return true;
        } finally {
            lock.unlock();
        }
    }

    protected boolean isValidStatusChange(Status starting, Status newStatus) {
        var validStatus = VALID_STATUS_CHANGE.getOrDefault(starting, Collections.emptySet());
        return validStatus.contains(newStatus);
    }
}

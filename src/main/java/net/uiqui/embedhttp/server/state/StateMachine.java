package net.uiqui.embedhttp.server.state;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.emptySet;

public class StateMachine {
    private static final Map<ServerState, Set<ServerState>> VALID_STATE_CHANGE = Map.of(
            ServerState.STARTING, Set.of(ServerState.RUNNING, ServerState.STOPPED),
            ServerState.RUNNING, Set.of(ServerState.STOPPING, ServerState.STOPPED),
            ServerState.STOPPING, Set.of(ServerState.STOPPED),
            ServerState.STOPPED, Set.of(ServerState.STARTING)
    );

    private final Lock lock = new ReentrantLock();
    private final Condition stateChanged = lock.newCondition();

    private ServerState currentServerState;

    public StateMachine(ServerState initialServerState) {
        this.currentServerState = initialServerState;
    }

    public ServerState getCurrentState() {
        lock.lock();
        try {
            return currentServerState;
        } finally {
            lock.unlock();
        }
    }

    public ServerState waitForState(ServerState... serverStates) throws InterruptedException {
        var wantedState = Set.of(serverStates);

        lock.lock();
        try {
            while (!wantedState.contains(currentServerState)) {
                stateChanged.await();
            }

            return currentServerState;
        } finally {
            lock.unlock();
        }
    }

    public boolean setState(ServerState newServerState) {
        lock.lock();
        try {
            if (!isValidStateChange(currentServerState, newServerState)) {
                return false;
            }

            currentServerState = newServerState;
            stateChanged.signalAll();

            return true;
        } finally {
            lock.unlock();
        }
    }

    protected boolean isValidStateChange(ServerState currentServerState, ServerState newServerState) {
        var validState = VALID_STATE_CHANGE.getOrDefault(currentServerState, emptySet());
        return validState.contains(newServerState);
    }
}

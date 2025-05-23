package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.HttpServer;
import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.server.state.ServerState;
import net.uiqui.embedhttp.server.state.StateMachine;

import java.lang.System.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

public abstract class ServerInstance implements HttpServer {
    protected static final Logger logger = System.getLogger(ServerInstance.class.getName());

    protected final StateMachine stateMachine = new StateMachine(ServerState.STOPPED);
    protected final AtomicInteger instancePort = new AtomicInteger(-1);
    protected final int port;
    protected final int backlog;

    protected ServerInstance(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    public boolean start(Router router) throws InterruptedException {
        if (stateMachine.getCurrentState() == ServerState.RUNNING) {
            logger.log(DEBUG, () -> serverLogMessage("Already running"));
            return true;
        }

        if (stateMachine.getCurrentState() != ServerState.STOPPED) {
            logger.log(WARNING, () -> serverLogMessage("Is not stopped"));
            return false;
        }

        if (!stateMachine.setState(ServerState.STARTING)) {
            logger.log(WARNING, () -> serverLogMessage("Can''t be started"));
            return false;
        }

        Thread.ofPlatform()
                .daemon(false)
                .start(() -> listenAndServe(router));

        logger.log(DEBUG, () -> serverLogMessage("Waiting for server to start"));
        var newState = stateMachine.waitForState(ServerState.RUNNING, ServerState.STOPPED);
        return newState == ServerState.RUNNING;
    }

    public abstract void listenAndServe(Router router);

    public boolean stop() throws InterruptedException {
        if (stateMachine.getCurrentState() == ServerState.STOPPED) {
            logger.log(DEBUG, () -> serverLogMessage("Is already stopped"));
            return true;
        }

        if (stateMachine.getCurrentState() != ServerState.RUNNING) {
            logger.log(WARNING, () -> serverLogMessage("Is not running"));
            return false;
        }

        if (!stateMachine.setState(ServerState.STOPPING)) {
            logger.log(WARNING, () -> serverLogMessage("Can''t be stopped"));
            return false;
        }

        logger.log(DEBUG, () -> serverLogMessage("Waiting for server to stop"));
        stateMachine.waitForState(ServerState.STOPPED);
        logger.log(INFO, () -> serverLogMessage("Stopped"));

        return true;
    }

    public boolean isRunning() {
        return stateMachine.getCurrentState() == ServerState.RUNNING;
    }

    public int getInstancePort() {
        if (stateMachine.getCurrentState() != ServerState.RUNNING) {
            return -1;
        }

        return instancePort.get();
    }

    protected String serverLogMessage(String message, Object... args) {
        var finalMessage = args.length > 0
                ? String.format(message, args)
                : message;

        return String.format("Instance(%d): %s", port, finalMessage);
    }
}
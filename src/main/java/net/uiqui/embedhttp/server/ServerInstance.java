package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.HttpServer;
import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.routing.RouterImpl;
import net.uiqui.embedhttp.server.state.ServerState;
import net.uiqui.embedhttp.server.state.StateMachine;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

public class ServerInstance implements HttpServer {
    private static final Logger logger = System.getLogger(ServerInstance.class.getName());
    public static final int SO_TIMEOUT = 1000;

    private final StateMachine stateMachine = new StateMachine(ServerState.STOPPED);
    private final AtomicInteger instancePort = new AtomicInteger(-1);
    private final int port;
    private final int backlog;

    public ServerInstance(int port, int backlog) {
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

        Thread.ofPlatform().daemon(false).start(() -> {
            try (var serverSocket = new ServerSocket(port, backlog)) {
                instancePort.set(serverSocket.getLocalPort());
                serverSocket.setSoTimeout(SO_TIMEOUT);

                var requestParser = new RequestParser();
                var responseWriter = new ResponseWriter();
                var requestProcessor = new RequestProcessor(requestParser, responseWriter, (RouterImpl) router);

                stateMachine.setState(ServerState.RUNNING);
                logger.log(INFO, () -> serverLogMessage("Started on port " + serverSocket.getLocalPort()));

                try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
                    while (stateMachine.getCurrentState() == ServerState.RUNNING) {
                        acceptAndProcess(serverSocket, executorService, requestProcessor);
                    }
                }
            } catch (Exception e) {
                logger.log(ERROR, () -> serverLogMessage("Error starting server"), e);
            } finally {
                stateMachine.setState(ServerState.STOPPED);
            }
        });

        logger.log(DEBUG, () -> serverLogMessage("Waiting for server to start"));
        var newState = stateMachine.waitForState(ServerState.RUNNING, ServerState.STOPPED);
        return newState == ServerState.RUNNING;
    }

    private void acceptAndProcess(ServerSocket serverSocket, ExecutorService executorService, RequestProcessor requestProcessor) throws IOException {
        try {
            var clientSocket = serverSocket.accept();
            handleRequest(clientSocket, executorService, requestProcessor);
        } catch (SocketTimeoutException e) {
            // Ignore timeout exception
        } catch (SocketException e) {
            logger.log(ERROR, () -> serverLogMessage("Error accepting client requests"), e);
        }
    }

    private void handleRequest(Socket clientSocket, ExecutorService executorService, RequestProcessor requestProcessor) {
        executorService.submit(() -> {
            try (clientSocket) {
                requestProcessor.process(clientSocket);
            } catch (Exception e) {
                logger.log(ERROR, () -> serverLogMessage("Error processing request"), e);
            }
        });
    }

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

    private String serverLogMessage(String message) {
        return String.format("Server(%d): %s", port, message);
    }
}
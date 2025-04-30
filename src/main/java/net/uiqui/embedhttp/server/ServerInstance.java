package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.HttpServer;
import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.impl.RouterImpl;
import net.uiqui.embedhttp.server.state.ServerState;
import net.uiqui.embedhttp.server.state.StateMachine;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerInstance implements HttpServer {
    private static final Logger logger = Logger.getLogger(ServerInstance.class.getName());
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
            logger.log(Level.FINER, "Server(" + port + ") is already running");
            return true;
        }

        if (stateMachine.getCurrentState() != ServerState.STOPPED) {
            logger.log(Level.WARNING, "Server(" + port + ") is not stopped");
            return false;
        }

        if (!stateMachine.setState(ServerState.STARTING)) {
            logger.log(Level.WARNING, "Server(" + port + ") can't be started");
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
                logger.log(Level.INFO, "Server(" + port + ") started on port " + serverSocket.getLocalPort());

                while (stateMachine.getCurrentState() == ServerState.RUNNING) {
                    try {
                        var clientSocket = serverSocket.accept();
                        handleRequest(clientSocket, requestProcessor);
                    } catch (SocketTimeoutException e) {
                        // Ignore timeout exception
                    } catch (SocketException e) {
                        logger.log(Level.SEVERE, "Server(" + port + "): Error accepting client requests", e);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Server(" + port + "): Error starting server", e);
            } finally {
                stateMachine.setState(ServerState.STOPPED);
            }
        });

        logger.log(Level.FINER, "Waiting for server(" + port + ") to start");
        var newState = stateMachine.waitForState(ServerState.RUNNING, ServerState.STOPPED);
        return newState == ServerState.RUNNING;
    }

    private void handleRequest(Socket clientSocket, RequestProcessor requestProcessor) {
        Thread.ofVirtual().start(() -> {
            try {
                requestProcessor.process(clientSocket);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Server(" + port + "): Error processing request", e);
            }
        });
    }

    public boolean stop() throws InterruptedException {
        if (stateMachine.getCurrentState() == ServerState.STOPPED) {
            logger.log(Level.FINER, "Server(" + port + ") is already stopped");
            return true;
        }

        if (stateMachine.getCurrentState() != ServerState.RUNNING) {
            logger.log(Level.WARNING, "Server(" + port + ") is not running");
            return false;
        }

        if (!stateMachine.setState(ServerState.STOPPING)) {
            logger.log(Level.WARNING, "Server(" + port + ") can't be stopped");
            return false;
        }

        logger.log(Level.FINER, "Waiting for server(" + port + ") to stop");
        stateMachine.waitForState(ServerState.STOPPED);
        logger.log(Level.INFO, "Server(" + port + ") stopped");

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
}
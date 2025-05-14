package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.routing.RouterImpl;
import net.uiqui.embedhttp.server.ServerInstance;
import net.uiqui.embedhttp.server.state.ServerState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class IOServer extends ServerInstance {
    public static final int SO_TIMEOUT = 1000;

    public IOServer(int port, int backlog) {
        super(port, backlog);
    }

    @Override
    public void listenAndServe(Router router) {
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
}

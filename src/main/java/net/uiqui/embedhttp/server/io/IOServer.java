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

import static java.lang.System.Logger.Level.DEBUG;
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
            logger.log(INFO, () -> serverLogMessage("Started on port %d", serverSocket.getLocalPort()));

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
            executorService.submit(() -> handleRequest(clientSocket, requestProcessor));
        } catch (SocketTimeoutException e) {
            // Ignore timeout exception
        } catch (SocketException e) {
            logger.log(ERROR, () -> serverLogMessage("Error accepting client requests"), e);
        }
    }

    private void handleRequest(Socket clientSocket, RequestProcessor requestProcessor) {
        var clientAddress = clientSocket.getInetAddress().getHostAddress();
        var clientPort = clientSocket.getPort();

        logger.log(DEBUG, () -> serverLogMessage("Client(%s:%d): Connected", clientAddress, clientPort));

        try (clientSocket) {
            clientSocket.setSoTimeout(SO_TIMEOUT);
            var keepAlive = true;

            while (keepAlive && stateMachine.getCurrentState() == ServerState.RUNNING) {
                keepAlive = requestProcessor.process(clientSocket);
            }

            logger.log(DEBUG, () -> serverLogMessage("Client(%s:%d): Connection closed", clientAddress, clientPort));
        } catch (ClientDisconnectedException e) {
            logger.log(DEBUG, () -> serverLogMessage("Client(%s:%d): Disconnected", clientAddress, clientPort));
        } catch (Exception e) {
            logger.log(ERROR, () -> serverLogMessage("Client(%s:%d): Error processing request"), e);
        }
    }
}

package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.server.state.ServerState;
import net.uiqui.embedhttp.server.state.StateMachine;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerInstance {
    private final StateMachine stateMachine = new StateMachine(ServerState.STOPPED);
    private final int port;
    private final int backlog;

    public ServerInstance(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    public boolean start(Router router) throws Exception {
        if (stateMachine.getCurrentState() == ServerState.RUNNING) {
            return true;
        }

        if (stateMachine.getCurrentState() != ServerState.STOPPED) {
            return false;
        }

        if (!stateMachine.setState(ServerState.STARTING)) {
            return false;
        }

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port, backlog)) {
                stateMachine.setState(ServerState.RUNNING);

                while (stateMachine.getCurrentState() == ServerState.RUNNING) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleRequest(clientSocket, router);
                    } catch (SocketException e) {
                        throw e;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stateMachine.setState(ServerState.STOPPED);
            }
        }).start();

        var newState = stateMachine.waitForState(ServerState.RUNNING, ServerState.STOPPED);
        return newState == ServerState.RUNNING;
    }

    private void handleRequest(Socket clientSocket, Router router) {

    }

    public boolean stop() throws InterruptedException {
        if (stateMachine.getCurrentState() == ServerState.STOPPED) {
            return true;
        }

        if (stateMachine.getCurrentState() != ServerState.RUNNING) {
            return false;
        }

        if (!stateMachine.setState(ServerState.STOPPING)) {
            return false;
        }

        stateMachine.waitForState(ServerState.STOPPED);
        return true;
    }
}
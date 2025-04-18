package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.routing.Router;
import net.uiqui.embedhttp.server.status.Status;
import net.uiqui.embedhttp.server.status.StatusHolder;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerInstance {
    private final StatusHolder serverStatus = new StatusHolder(Status.STOPPED);
    private final int port;
    private final int backlog;

    public ServerInstance(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    public boolean start(Router router) throws Exception {
        if (serverStatus.getCurrentStatus() == Status.RUNNING) {
            return true;
        }

        if (serverStatus.getCurrentStatus() != Status.STOPPED) {
            return false;
        }

        if (serverStatus.setStatus(Status.STARTING)) {
            return false;
        }

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port, backlog)) {
                serverStatus.setStatus(Status.RUNNING);

                while (serverStatus.getCurrentStatus() == Status.RUNNING) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleRequest(clientSocket, router);
                    } catch (SocketException e) {
                        if (!serverSocket.isClosed()) throw e;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                serverStatus.setStatus(Status.STOPPED);
            }
        }).start();

        var newstatus = serverStatus.waitForStatus(Status.RUNNING, Status.STOPPED);
        return newstatus == Status.RUNNING;
    }

    private void handleRequest(Socket clientSocket, Router router) {

    }

    public boolean stop() throws InterruptedException {
        if (serverStatus.getCurrentStatus() == Status.STOPPED) {
            return true;
        }

        if (serverStatus.getCurrentStatus() != Status.RUNNING) {
            return false;
        }

        if (!serverStatus.setStatus(Status.STOPPING)) {
            return false;
        }

        serverStatus.waitForStatus(Status.STOPPED);
        return true;
    }
}
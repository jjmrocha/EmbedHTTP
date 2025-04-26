package net.uiqui.embedhttp;

import net.uiqui.embedhttp.routing.Router;
import net.uiqui.embedhttp.server.ServerInstance;

import static java.util.Objects.requireNonNull;

public class HttpServer {
    private static final int DEFAULT_PORT = 0;
    private static final int DEFAULT_BACKLOG = 10;

    private final ServerInstance serverInstance;

    public HttpServer() {
        this(DEFAULT_PORT, DEFAULT_BACKLOG);
    }

    public HttpServer(int port, int backlog) {
        serverInstance = new ServerInstance(port, backlog);
    }

    public boolean start(Router router) {
        requireNonNull(router, "Router cannot be null");
        try {
            return serverInstance.start(router);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean stop() {
        try {
            return serverInstance.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

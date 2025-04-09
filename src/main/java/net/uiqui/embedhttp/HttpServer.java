package net.uiqui.embedhttp;

import net.uiqui.embedhttp.server.ServerInstance;

import java.util.List;

import static java.util.Arrays.asList;
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

    public boolean start(List<HttpRequestHandler> handlers) {
        requireNonNull(handlers, "Handlers cannot be null");
        try {
            return serverInstance.start(handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean start(HttpRequestHandler... handlers) {
        return start(asList(handlers));
    }

    public boolean stop() {
        try {
            return serverInstance.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

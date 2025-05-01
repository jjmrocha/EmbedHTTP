package net.uiqui.embedhttp;

import net.uiqui.embedhttp.server.ServerInstance;

/**
 * Interface for an HTTP server.
 * <p>
 * This interface defines the methods required to start and stop an HTTP server, as well as to retrieve the port on which
 * the server is running.
 * </p>
 */
public interface HttpServer {
    int DEFAULT_BACKLOG = 100;

    /**
     * Starts the HTTP server with the specified router.
     *
     * @param router The router to handle incoming requests.
     * @return true if the server started successfully, false otherwise.
     * @throws Exception If an error occurs while starting the server.
     */
    boolean start(Router router) throws Exception;

    /**
     * Stops the HTTP server.
     *
     * @return true if the server stopped successfully, false otherwise.
     * @throws Exception If an error occurs while stopping the server.
     */
    boolean stop() throws Exception;

    /**
     * Retrieves the port on which the server is running.
     *
     * @return The port number.
     */
    int getInstancePort();

    /**
     * Checks if the server is currently running.
     *
     * @return true if the server is running, false otherwise.
     */
    boolean isRunning();

    /**
     * Creates a new instance of the HTTP server with the specified port and default backlog.
     *
     * @param port The port on which the server will listen for incoming connections.
     * @return A new instance of the HTTP server.
     */
    static HttpServer newInstance(int port) {
        return new ServerInstance(port, DEFAULT_BACKLOG);
    }
}

package net.uiqui.embedhttp.api;

/**
 * Interface representing an HTTP request handler.
 * Implementations of this interface define how to handle HTTP requests.
 */
@FunctionalInterface
public interface HttpRequestHandler {
    /**
     * Interface representing an HTTP request handler.
     * Implementations of this interface define how to handle HTTP requests.
     */
    void handle(HttpRequest request, HttpResponse response);
}

package net.uiqui.embedhttp.api;

@FunctionalInterface
public interface HttpRequestHandler {
    void handle(HttpRequest request, HttpResponse response);
}

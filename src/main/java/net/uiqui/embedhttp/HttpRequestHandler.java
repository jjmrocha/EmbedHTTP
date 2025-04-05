package net.uiqui.embedhttp;

public interface HttpRequestHandler {
    void handle(HttpRequest request, HttpResponse response);
}

package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.impl.HttpRequestImpl;
import net.uiqui.embedhttp.api.impl.HttpResponseImpl;
import net.uiqui.embedhttp.api.impl.RouterImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Socket;

public class RequestProcessor {
    private final RequestParser requestParser;
    private final ResponseWriter responseWriter;
    private final RouterImpl router;

    public RequestProcessor(RequestParser requestParser, ResponseWriter responseWriter, RouterImpl router) {
        this.requestParser = requestParser;
        this.responseWriter = responseWriter;
        this.router = router;
    }

    public void process(Socket clientSocket) throws IOException {
        var chainedRequest = parse(clientSocket.getInputStream());
        var chainedHttpRequest = route(chainedRequest);
        var response = execute(chainedHttpRequest);
        write(response, clientSocket.getOutputStream());
    }


    private ChainedValue<Request> parse(InputStream inputStream) throws IOException {
        try {
            var request = requestParser.parseRequest(inputStream);
            return new ChainedValue<>(request, null);
        } catch (ProtocolException e) {
            var response = HttpResponse.badRequest(ContentType.TEXT_PLAIN, "Bad Request: " + e.getMessage());
            return new ChainedValue<>(null, response);
        }
    }

    private ChainedValue<HttpRequestImpl> route(ChainedValue<Request> chainedRequest) {
        if (chainedRequest.hasResponse()) {
            return new ChainedValue<>(null, chainedRequest.response());
        }

        var request = chainedRequest.value;
        var httpRequest = router.routeRequest(request);

        if (httpRequest == null) {
            var response = HttpResponse.notFound(ContentType.TEXT_PLAIN, "Not Found:" + request.getPath());
            return new ChainedValue<>(null, response);
        }

        return new ChainedValue<>(httpRequest, null);
    }

    private HttpResponse execute(ChainedValue<HttpRequestImpl> chainedHttpRequest) {
        if (chainedHttpRequest.hasResponse()) {
            return chainedHttpRequest.response();
        }

        var handler = chainedHttpRequest.value.route().getHandler();
        var httpRequest = chainedHttpRequest.value;

        try {
            return handler.handle(httpRequest);
        } catch (Throwable e) {
            return HttpResponse.unexpectedError(ContentType.TEXT_PLAIN, "Internal Server Error");
        }
    }

    private void write(HttpResponse response, OutputStream outputStream) throws IOException {
        var castedResponse = (HttpResponseImpl) response;
        responseWriter.writeResponse(outputStream, castedResponse);
    }

    private record ChainedValue<T>(T value, HttpResponse response) {
        public boolean hasResponse() {
            return response != null;
        }
    }
}

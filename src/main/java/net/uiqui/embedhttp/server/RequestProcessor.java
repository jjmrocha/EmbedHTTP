package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.impl.HttpRequestImpl;
import net.uiqui.embedhttp.api.impl.HttpResponseImpl;
import net.uiqui.embedhttp.routing.RouterImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.function.Function;

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
        var response = parse(clientSocket.getInputStream())
                .next(this::route)
                .then(this::execute);
        write(response, clientSocket.getOutputStream());
    }

    private BreakableSequence<Request> parse(InputStream inputStream) throws IOException {
        try {
            var request = requestParser.parseRequest(inputStream);
            return new BreakableSequence<>(request, null);
        } catch (ProtocolException e) {
            var response = HttpResponse.badRequest()
                    .setBody(ContentType.TEXT_PLAIN, "Bad Request: " + e.getMessage());
            return new BreakableSequence<>(null, response);
        }
    }

    private BreakableSequence<HttpRequestImpl> route(Request request) {
        var httpRequest = router.routeRequest(request);

        if (httpRequest == null) {
            var response = HttpResponse.notFound()
                    .setBody(ContentType.TEXT_PLAIN, "Not Found:" + request.getPath());
            return new BreakableSequence<>(null, response);
        }

        return new BreakableSequence<>(httpRequest, null);
    }

    private HttpResponse execute(HttpRequestImpl httpRequest) {
        var handler = httpRequest.getRoute().getHandler();

        try {
            return handler.handle(httpRequest);
        } catch (Exception e) {
            return HttpResponse.unexpectedError()
                    .setBody(ContentType.TEXT_PLAIN, "Unexpected error executing request");
        }
    }

    private void write(HttpResponse response, OutputStream outputStream) throws IOException {
        var castedResponse = (HttpResponseImpl) response;
        responseWriter.writeResponse(outputStream, castedResponse);
    }

    private record BreakableSequence<T>(T value, HttpResponse response) {
        public boolean hasResponse() {
            return response != null;
        }

        public <R> BreakableSequence<R> next(Function<T, BreakableSequence<R>> mapper) {
            if (hasResponse()) {
                return new BreakableSequence<>(null, response);
            }

            return mapper.apply(value);
        }

        public HttpResponse then(Function<T, HttpResponse> mapper) {
            if (hasResponse()) {
                return response;
            }

            return mapper.apply(value);
        }
    }
}

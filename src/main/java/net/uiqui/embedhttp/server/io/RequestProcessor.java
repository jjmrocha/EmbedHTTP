package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.impl.HttpRequestImpl;
import net.uiqui.embedhttp.api.impl.HttpResponseImpl;
import net.uiqui.embedhttp.routing.RouterImpl;
import net.uiqui.embedhttp.server.Request;
import net.uiqui.embedhttp.server.ResponsePipeline;

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
        var response = ResponsePipeline.of(clientSocket.getInputStream())
                .next(this::parse)
                .next(this::route)
                .then(this::execute);
        write(response, clientSocket.getOutputStream());
    }

    private ResponsePipeline<Request> parse(InputStream inputStream) {
        try {
            var request = requestParser.parseRequest(inputStream);
            return ResponsePipeline.of(request);
        } catch (ProtocolException e) {
            var response = HttpResponse.badRequest()
                    .setBody(ContentType.TEXT_PLAIN, "Bad Request: " + e.getMessage());
            return ResponsePipeline.reply(response);
        } catch (IOException e) {
            var response = HttpResponse.unexpectedError()
                    .setBody(ContentType.TEXT_PLAIN, "Something went on our side");
            return ResponsePipeline.reply(response);
        }
    }

    private ResponsePipeline<HttpRequestImpl> route(Request request) {
        var httpRequest = router.routeRequest(request);

        if (httpRequest == null) {
            var response = HttpResponse.notFound()
                    .setBody(ContentType.TEXT_PLAIN, "Not Found:" + request.getPath());
            return ResponsePipeline.reply(response);
        }

        return ResponsePipeline.of(httpRequest);
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
}

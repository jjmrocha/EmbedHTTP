package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.impl.HttpRequestImpl;
import net.uiqui.embedhttp.api.impl.HttpResponseImpl;
import net.uiqui.embedhttp.routing.RouterImpl;
import net.uiqui.embedhttp.server.ConnectionHeader;
import net.uiqui.embedhttp.server.Request;
import net.uiqui.embedhttp.server.RequestPipeline;

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

    public boolean process(Socket clientSocket) throws IOException {
        var response = RequestPipeline.of(clientSocket.getInputStream())
                .map(this::parse)
                .map(this::route)
                .then(this::execute);
        write(response, clientSocket.getOutputStream());

        return shouldKeepAliveConnection(response);
    }

    private boolean shouldKeepAliveConnection(HttpResponse response) {
        var castedResponse = (HttpResponseImpl) response;
        return !castedResponse.closeConnection();
    }

    private RequestPipeline<Request> parse(InputStream inputStream) throws ClientDisconnectedException {
        try {
            var request = requestParser.parseRequest(inputStream);
            return RequestPipeline.of(request);
        } catch (ProtocolException e) {
            var response = HttpResponse.badRequest()
                    .setBody(ContentType.TEXT_PLAIN, "Bad Request: " + e.getMessage());
            return RequestPipeline.reply(response);
        } catch (ClientDisconnectedException e) {
            throw e;
        } catch (IOException e) {
            var response = HttpResponse.unexpectedError()
                    .setBody(ContentType.TEXT_PLAIN, "Something went on our side");
            return RequestPipeline.reply(response);
        }
    }

    private RequestPipeline<HttpRequestImpl> route(Request request) {
        var httpRequest = router.routeRequest(request);

        if (httpRequest == null) {
            var response = HttpResponse.notFound()
                    .setBody(ContentType.TEXT_PLAIN, "Not Found:" + request.getPath());
            return RequestPipeline.reply(response);
        }

        return RequestPipeline.of(httpRequest);
    }

    private HttpResponse execute(HttpRequestImpl httpRequest) {
        var handler = httpRequest.getRoute().getHandler();

        try {
            var response = handler.handle(httpRequest);

            if (!httpRequest.getRequest().isKeepAlive()) {
                response.setHeader(HttpHeader.CONNECTION, ConnectionHeader.CLOSE.getValue());
            }

            return response;
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

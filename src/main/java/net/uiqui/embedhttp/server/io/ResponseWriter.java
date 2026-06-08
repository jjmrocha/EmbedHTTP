package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.impl.HttpResponseImpl;
import net.uiqui.embedhttp.server.DateHeader;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseWriter {
    public static final String HTTP_VERSION_1_1 = "HTTP/1.1";
    public static final String CRLF = "\r\n";

    private final DateHeader dateHeader = new DateHeader();

    public void writeResponse(OutputStream outputStream, HttpResponseImpl response) throws IOException {
        var builder = new StringBuilder();

        // Write the HTTP response line
        builder.append(HTTP_VERSION_1_1)
                .append(" ")
                .append(response.getStatusCode())
                .append(" ")
                .append(response.getStatusMessage())
                .append(CRLF);

        // Write the headers
        for (var header : response.getHeaders().entrySet()) {
            builder.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append(CRLF);
        }

        // Ensure a definite message framing on keep-alive connections: a response with no body
        // still needs Content-Length (except where the status forbids a body).
        if (response.getBody() == null
                && statusAllowsBody(response.getStatusCode())
                && !response.getHeaders().containsKey(HttpHeader.CONTENT_LENGTH.getValue())) {
            builder.append(HttpHeader.CONTENT_LENGTH.getValue())
                    .append(": 0")
                    .append(CRLF);
        }

        // Write the Date header
        builder.append(HttpHeader.DATE.getValue())
                .append(": ")
                .append(dateHeader.getDateHeaderValue())
                .append(CRLF);

        // End of headers
        builder.append(CRLF);

        // Write the body if present
        if (response.getBody() != null) {
            builder.append(response.getBody());
        }

        // Write and flush the output stream to ensure all data is sent
        outputStream.write(builder.toString().getBytes());
        outputStream.flush();
    }

    private boolean statusAllowsBody(int statusCode) {
        // 1xx informational, 204 No Content and 304 Not Modified must not carry a message body.
        if (statusCode >= 100 && statusCode < 200) {
            return false;
        }

        return statusCode != 204 && statusCode != 304;
    }
}

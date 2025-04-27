package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.impl.HttpResponseImpl;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseWriter {
    public static final String HTTP_VERSION_1_1 = "HTTP/1.1";
    public static final String CRLF = "\r\n";

    public static void writeResponse(OutputStream outputStream, HttpResponseImpl response) throws IOException {
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

        // Write close connection header if not already set
        if (!response.getHeaders().containsKey(HttpHeader.CONNECTION.getValue())) {
            builder.append(HttpHeader.CONNECTION.getValue())
                    .append(": close")
                    .append(CRLF);
        }

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
}

package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.server.InsensitiveMap;
import net.uiqui.embedhttp.server.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static net.uiqui.embedhttp.server.ConnectionHeader.CLOSE;
import static net.uiqui.embedhttp.server.ConnectionHeader.KEEP_ALIVE;

public class RequestParser {
    private static final String TRANSFER_ENCODING_CHUNKED = "chunked";
    private static final int MAX_BODY_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_CHUNK_SIZE = 1024 * 1024; // 1MB
    private static final int MAX_HEADER_COUNT = 100;
    private static final int MAX_HEADER_SIZE = 8192; // 8KB

    public Request parseRequest(InputStream inputStream) throws IOException {
        return parseRequest(new HttpConnectionReader(inputStream));
    }

    public Request parseRequest(HttpConnectionReader reader) throws IOException {
        var requestLine = decodeRequestLine(reader);
        var headers = decodeRequestHeaders(reader);
        var body = decodeRequestBody(reader, headers);
        var keepAlive = decodeKeepAlive(headers);

        return new Request(requestLine.method(), requestLine.url(), headers, body, keepAlive);
    }

    private RequestLine decodeRequestLine(HttpConnectionReader reader) throws IOException {
        var line = readRequestLine(reader);

        var parts = line.split(" ", 3);
        if (parts.length != 3) {
            throw new ProtocolException("Invalid request line: " + line);
        }

        var method = HttpMethod.fromString(parts[0]);
        if (method == null) {
            throw new ProtocolException("Invalid HTTP method: " + parts[0]);
        }

        var url = parts[1];

        var version = HttpVersion.fromString(parts[2]);
        if (version == null) {
            throw new ProtocolException("Unsupported HTTP version: " + parts[2]);
        }

        return new RequestLine(method, url, version);
    }

    private static String readRequestLine(HttpConnectionReader reader) throws IOException {
        try {
            var line = reader.readLine();
            if (line == null) {
                throw new ClientDisconnectedException();
            }

            if (line.isEmpty()) {
                throw new ProtocolException("Invalid request line: line is empty");
            }

            return line;
        } catch (SocketTimeoutException e) {
            throw new ClientDisconnectedException(e);
        }
    }

    private InsensitiveMap decodeRequestHeaders(HttpConnectionReader reader) throws IOException {
        var headers = new InsensitiveMap();
        String line;
        int headerCount = 0;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.length() > MAX_HEADER_SIZE) {
                throw new ProtocolException("Header too large: maximum " + MAX_HEADER_SIZE + " bytes allowed");
            }

            var colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                throw new ProtocolException("Invalid header line: " + line);
            }

            headerCount++;
            if (headerCount > MAX_HEADER_COUNT) {
                throw new ProtocolException("Too many headers: maximum " + MAX_HEADER_COUNT + " allowed");
            }

            var headerName = line.substring(0, colonIndex).trim();
            var headerValue = line.substring(colonIndex + 1).trim();
            headers.put(headerName, headerValue);
        }

        return headers;
    }

    private String decodeRequestBody(HttpConnectionReader reader, Map<String, String> headers) throws IOException {
        if (headers.containsKey(HttpHeader.CONTENT_LENGTH.getValue())) {
            var contentLength = Integer.parseInt(headers.get(HttpHeader.CONTENT_LENGTH.getValue()));
            if (contentLength > MAX_BODY_SIZE) {
                throw new ProtocolException("Request body too large: " + contentLength);
            }

            return new String(reader.readBody(contentLength), StandardCharsets.UTF_8);
        }

        if (TRANSFER_ENCODING_CHUNKED.equalsIgnoreCase(headers.get(HttpHeader.TRANSFER_ENCODING.getValue()))) {
            return readChunkedBody(reader);
        }

        return ""; // No body or unsupported format
    }

    private boolean decodeKeepAlive(InsensitiveMap headers) {
        var connectionHeader = headers.get(HttpHeader.CONNECTION.getValue());
        if (connectionHeader == null) {
            return true; // Default to keep-alive if no connection header is present
        }

        if (KEEP_ALIVE.getValue().equalsIgnoreCase(connectionHeader)) {
            return true;
        }

        return !CLOSE.getValue().equalsIgnoreCase(connectionHeader);
    }

    private String readChunkedBody(HttpConnectionReader reader) throws IOException {
        var body = new ByteArrayOutputStream();

        while (true) {
            int chunkSize = readChunkSize(reader);
            if (chunkSize == 0) {
                consumeTrailingLine(reader); // Consume the trailing empty line
                break;
            }

            body.writeBytes(reader.readBody(chunkSize));
            consumeTrailingLine(reader); // Consume trailing \r\n
        }

        return body.toString(StandardCharsets.UTF_8);
    }

    private int readChunkSize(HttpConnectionReader reader) throws IOException {
        var line = reader.readLine();
        if (line == null) {
            throw new ProtocolException("Unexpected end of stream while reading chunk size");
        }

        int chunkSize = Integer.parseInt(line.trim(), 16);
        if (chunkSize > MAX_CHUNK_SIZE) {
            throw new ProtocolException("Chunk size too large: " + chunkSize);
        }

        return chunkSize;
    }

    private void consumeTrailingLine(HttpConnectionReader reader) throws IOException {
        var line = reader.readLine();
        if (line == null) {
            throw new ProtocolException("Unexpected end of stream while consuming trailing line");
        }
    }

    protected record RequestLine(HttpMethod method, String url, HttpVersion version) {
    }
}

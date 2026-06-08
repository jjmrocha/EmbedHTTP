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

    public Request parseRequest(InputStream inputStream) throws IOException {
        return parseRequest(new HttpConnectionReader(inputStream));
    }

    public Request parseRequest(HttpConnectionReader reader) throws IOException {
        var requestLine = decodeRequestLine(reader);
        var headers = decodeRequestHeaders(reader);
        validateHost(requestLine.version(), headers);
        var body = decodeRequestBody(reader, headers);
        var keepAlive = decodeKeepAlive(requestLine.version(), headers);

        return new Request(requestLine.method(), requestLine.url(), headers, body, keepAlive);
    }

    private void validateHost(HttpVersion version, InsensitiveMap headers) throws ProtocolException {
        // RFC 9112 §3.2: an HTTP/1.1 request MUST carry a Host header (duplicates are rejected during header parsing).
        if (version == HttpVersion.VERSION_1_1 && !headers.containsKey(HttpHeader.HOST.getValue())) {
            throw new ProtocolException("Missing Host header");
        }
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

            // RFC 9112 §3.2: more than one Host header is a request-smuggling signal and must be rejected.
            if (HttpHeader.HOST.getValue().equalsIgnoreCase(headerName) && headers.containsKey(headerName)) {
                throw new ProtocolException("Duplicate Host header");
            }

            headers.put(headerName, headerValue);
        }

        return headers;
    }

    private String decodeRequestBody(HttpConnectionReader reader, Map<String, String> headers) throws IOException {
        var hasContentLength = headers.containsKey(HttpHeader.CONTENT_LENGTH.getValue());
        var hasTransferEncoding = headers.containsKey(HttpHeader.TRANSFER_ENCODING.getValue());

        // RFC 9112 §6.1: a message with both framing headers is a request-smuggling vector.
        if (hasContentLength && hasTransferEncoding) {
            throw new ProtocolException("Content-Length and Transfer-Encoding must not both be present");
        }

        if (hasContentLength) {
            var contentLength = parseContentLength(headers.get(HttpHeader.CONTENT_LENGTH.getValue()));
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

    private int parseContentLength(String value) throws ProtocolException {
        int contentLength;
        try {
            contentLength = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid Content-Length: " + value);
        }

        if (contentLength < 0) {
            throw new ProtocolException("Invalid Content-Length: " + value);
        }

        return contentLength;
    }

    private boolean decodeKeepAlive(HttpVersion version, InsensitiveMap headers) {
        var connectionHeader = headers.get(HttpHeader.CONNECTION.getValue());

        if (KEEP_ALIVE.getValue().equalsIgnoreCase(connectionHeader)) {
            return true;
        }

        if (CLOSE.getValue().equalsIgnoreCase(connectionHeader)) {
            return false;
        }

        // No explicit directive: HTTP/1.1 defaults to keep-alive, HTTP/1.0 to close.
        return version == HttpVersion.VERSION_1_1;
    }

    private String readChunkedBody(HttpConnectionReader reader) throws IOException {
        var body = new ByteArrayOutputStream();

        while (true) {
            int chunkSize = readChunkSize(reader);
            if (chunkSize == 0) {
                consumeTrailingLine(reader); // Consume the trailing empty line
                break;
            }

            // Cap the aggregate body size; per-chunk limits alone leave chunked transfers unbounded.
            if (body.size() + chunkSize > MAX_BODY_SIZE) {
                throw new ProtocolException("Request body too large: exceeds " + MAX_BODY_SIZE + " bytes");
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

        // A chunk-size line may carry extensions after a ';' (RFC 9112 §7.1.1); ignore them.
        var sizeToken = line.trim();
        var extensionIndex = sizeToken.indexOf(';');
        if (extensionIndex != -1) {
            sizeToken = sizeToken.substring(0, extensionIndex).trim();
        }

        int chunkSize;
        try {
            chunkSize = Integer.parseInt(sizeToken, 16);
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid chunk size: " + line);
        }

        if (chunkSize < 0) {
            throw new ProtocolException("Invalid chunk size: " + line);
        }

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

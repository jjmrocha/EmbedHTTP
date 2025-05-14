package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RequestParser {
    private static final String TRANSFER_ENCODING_CHUNKED = "chunked";
    private static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    private static final String CONNECTION_CLOSE = "close";

    public Request parseRequest(InputStream inputStream) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        var requestLine = decodeRequestLine(reader);
        var headers = decodeRequestHeaders(reader);
        var body = decodeRequestBody(reader, headers);
        var keepAlive = decodeKeepAlive(headers);

        return new Request(requestLine.method(), requestLine.url(), headers, body, keepAlive);
    }

    private RequestLine decodeRequestLine(BufferedReader reader) throws IOException {
        var line = reader.readLine();
        if (line == null || line.isEmpty()) {
            throw new ProtocolException("Invalid request line: line is null or empty");
        }

        var parts = line.split(" ", 3);
        if (parts.length != 3) {
            throw new ProtocolException("Invalid request line: " + line);
        }

        var method = HttpMethod.fromString(parts[0]);
        if (method == null) {
            throw new ProtocolException("Invalid HTTP method: " + parts[0]);
        }

        var url = parts[1];
        var version = parts[2];
        return new RequestLine(method, url, version);
    }

    private InsensitiveMap decodeRequestHeaders(BufferedReader reader) throws IOException {
        var headers = new InsensitiveMap();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            var colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                throw new ProtocolException("Invalid header line: " + line);
            }

            var headerName = line.substring(0, colonIndex).trim();
            var headerValue = line.substring(colonIndex + 1).trim();
            headers.put(headerName, headerValue);
        }

        return headers;
    }

    private String decodeRequestBody(BufferedReader reader, Map<String, String> headers) throws IOException {
        if (headers.containsKey(HttpHeader.CONTENT_LENGTH.getValue())) {
            var contentLength = Integer.parseInt(headers.get(HttpHeader.CONTENT_LENGTH.getValue()));
            return readFixedSizeBodyChunk(reader, contentLength);
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

        if (CONNECTION_KEEP_ALIVE.equalsIgnoreCase(connectionHeader)) {
            return true;
        }

        return !CONNECTION_CLOSE.equalsIgnoreCase(connectionHeader);
    }

    private String readChunkedBody(BufferedReader reader) throws IOException {
        var body = new StringBuilder();

        while (true) {
            int chunkSize = readChunkSize(reader);
            if (chunkSize == 0) {
                consumeTrailingLine(reader); // Consume the trailing empty line
                break;
            }

            body.append(readFixedSizeBodyChunk(reader, chunkSize));
            consumeTrailingLine(reader); // Consume trailing \r\n
        }

        return body.toString();
    }

    private int readChunkSize(BufferedReader reader) throws IOException {
        var line = reader.readLine();
        if (line == null) {
            throw new ProtocolException("Unexpected end of stream while reading chunk size");
        }

        return Integer.parseInt(line.trim(), 16);
    }

    private String readFixedSizeBodyChunk(BufferedReader reader, int chunkSize) throws IOException {
        var chunk = new char[chunkSize];
        int read = 0;

        while (read < chunkSize) {
            var readCount = reader.read(chunk, read, chunkSize - read);
            if (readCount == -1) {
                throw new ProtocolException("Unexpected end of stream while reading body");
            }

            read += readCount;
        }

        return new String(chunk);
    }

    private void consumeTrailingLine(BufferedReader reader) throws IOException {
        var line = reader.readLine();
        if (line == null) {
            throw new ProtocolException("Unexpected end of stream while consuming trailing line");
        }
    }

    protected record RequestLine(HttpMethod method, String url, String version) {
    }
}
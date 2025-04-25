package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpMethod;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {
    public static Request parseRequest(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            var requestLine = decodeRequestLine(reader);
            var headers = decodeRequestHeaders(reader);
            var body = decodeRequestBody(reader, headers);

            return new Request(requestLine.method(), requestLine.url(), headers, body);
        }
    }

    private static RequestLine decodeRequestLine(BufferedReader reader) throws IOException {
        var line = reader.readLine();

        if (line == null || line.isEmpty()) {
            throw new IOException("Invalid request line: line is null or empty");
        }

        var parts = line.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request line: " + line);
        }

        var method = HttpMethod.fromString(parts[0]);
        if (method == null) {
            throw new IOException("Invalid HTTP method: " + parts[0]);
        }

        var url = parts[1];
        var version = parts[2];
        return new RequestLine(method, url, version);
    }

    private static Map<String, String> decodeRequestHeaders(BufferedReader reader) throws IOException {
        var headers = new HashMap<String, String>();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            var colonIndex = line.indexOf(":");
            if (colonIndex == -1) {
                throw new IOException("Invalid header line: " + line);
            }

            var headerName = line.substring(0, colonIndex).trim();
            var headerValue = line.substring(colonIndex + 1).trim();
            headers.put(headerName, headerValue);
        }

        return headers;
    }

    private static String decodeRequestBody(BufferedReader reader, Map<String, String> headers) throws IOException {
        if (headers.containsKey("Content-Length")) {
            var contentLength = Integer.parseInt(headers.get("Content-Length"));
            return readFixedSizeBodyChunk(reader, contentLength);
        }

        if ("chunked".equalsIgnoreCase(headers.get("Transfer-Encoding"))) {
            return readChunkedBody(reader);
        }

        return ""; // No body or unsupported format
    }

    private static String readChunkedBody(BufferedReader reader) throws IOException {
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

    private static int readChunkSize(BufferedReader reader) throws IOException {
        var line = reader.readLine();
        if (line == null) {
            throw new IOException("Unexpected end of stream while reading chunk size");
        }

        return Integer.parseInt(line.trim(), 16);
    }

    private static String readFixedSizeBodyChunk(BufferedReader reader, int chunkSize) throws IOException {
        var chunk = new char[chunkSize];
        int read = 0;

        while (read < chunkSize) {
            var readCount = reader.read(chunk, read, chunkSize - read);
            if (readCount == -1) {
                throw new IOException("Unexpected end of stream while reading body");
            }

            read += readCount;
        }

        return new String(chunk);
    }

    private static void consumeTrailingLine(BufferedReader reader) throws IOException {
        var line = reader.readLine();
        if (line == null) {
            throw new IOException("Unexpected end of stream while consuming trailing line");
        }
    }

    protected record RequestLine(HttpMethod method, String url, String version) {}
}
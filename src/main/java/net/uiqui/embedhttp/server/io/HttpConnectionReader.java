package net.uiqui.embedhttp.server.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;

/**
 * Byte-oriented reader over a connection's input stream.
 * <p>
 * A single instance is created per connection and reused across keep-alive requests, so bytes read
 * ahead while parsing one request remain buffered for the next. This is what makes pipelining work
 * and prevents the buffered-read-ahead loss of the previous per-request reader approach.
 * </p>
 * <p>
 * Lines (request line and headers) are decoded as ISO-8859-1 so that one byte maps to one char,
 * keeping size limits byte-accurate. Bodies are returned as raw bytes for the caller to decode.
 * </p>
 */
public class HttpConnectionReader {
    private static final int CR = '\r';
    private static final int LF = '\n';

    private final InputStream inputStream;

    public HttpConnectionReader(InputStream inputStream) {
        this.inputStream = new BufferedInputStream(inputStream);
    }

    /**
     * Reads a single line terminated by LF (an optional preceding CR is stripped) and returns it
     * without the terminator. Bytes after the terminator stay buffered for the next read.
     *
     * @return the line, or null if the end of the stream is reached before any byte is read.
     */
    public String readLine() throws IOException {
        var buffer = new ByteArrayOutputStream();
        int read;

        while ((read = inputStream.read()) != -1) {
            if (read == LF) {
                return toLine(buffer);
            }

            buffer.write(read);
        }

        if (buffer.size() == 0) {
            return null;
        }

        return buffer.toString(StandardCharsets.ISO_8859_1);
    }

    /**
     * Reads exactly {@code length} bytes from the stream.
     *
     * @param length the number of bytes to read.
     * @return a byte array of exactly {@code length} bytes.
     * @throws ProtocolException if the stream ends before {@code length} bytes are read.
     */
    public byte[] readBody(int length) throws IOException {
        var bytes = new byte[length];
        int read = 0;

        while (read < length) {
            var count = inputStream.read(bytes, read, length - read);
            if (count == -1) {
                throw new ProtocolException("Unexpected end of stream while reading body");
            }

            read += count;
        }

        return bytes;
    }

    private static String toLine(ByteArrayOutputStream buffer) {
        var bytes = buffer.toByteArray();
        var length = bytes.length;

        if (length > 0 && bytes[length - 1] == CR) {
            length--;
        }

        return new String(bytes, 0, length, StandardCharsets.ISO_8859_1);
    }
}

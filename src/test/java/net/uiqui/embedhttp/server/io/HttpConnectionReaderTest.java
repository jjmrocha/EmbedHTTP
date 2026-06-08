package net.uiqui.embedhttp.server.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

class HttpConnectionReaderTest {
    private HttpConnectionReader readerFor(String content) {
        var inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return new HttpConnectionReader(inputStream);
    }

    private HttpConnectionReader readerFor(byte[] content) {
        return new HttpConnectionReader(new ByteArrayInputStream(content));
    }

    @Test
    void testReadLineStripsCrlf() throws Exception {
        // given
        var reader = readerFor("GET /test HTTP/1.1\r\nHost: localhost\r\n");
        // when / then
        assertThat(reader.readLine()).isEqualTo("GET /test HTTP/1.1");
        assertThat(reader.readLine()).isEqualTo("Host: localhost");
    }

    @Test
    void testReadLineRejectsBareLf() {
        // given — HTTP framing requires CRLF; a bare LF is rejected (smuggling defense)
        var reader = readerFor("line-one\nline-two\n");
        // when
        var thrown = catchThrowable(reader::readLine);
        // then
        assertThat(thrown).isInstanceOf(ProtocolException.class);
    }

    @Test
    void testReadLineRejectsOverlongLine() {
        // given — a line with no terminator must not grow memory without bound
        var reader = readerFor("A".repeat(9000) + "\r\n");
        // when
        var thrown = catchThrowable(reader::readLine);
        // then
        assertThat(thrown).isInstanceOf(ProtocolException.class);
    }

    @Test
    void testReadLineReturnsNullAtEndOfStream() throws Exception {
        // given
        var reader = readerFor("");
        // when / then
        assertThat(reader.readLine()).isNull();
    }

    @Test
    void testReadBodyReturnsExactByteCount() throws Exception {
        // given
        var reader = readerFor("HelloWorld");
        // when
        var body = reader.readBody(5);
        // then
        assertThat(new String(body, StandardCharsets.UTF_8)).isEqualTo("Hello");
    }

    @Test
    void testReadLineThenBodyThenLineDoesNotOverRead() throws Exception {
        // given — a header line, then a 5-byte body, then a second header line
        var reader = readerFor("Header: one\r\nHELLOTrailer: two\r\n");
        // when
        var firstLine = reader.readLine();
        var body = reader.readBody(5);
        var secondLine = reader.readLine();
        // then
        assertThat(firstLine).isEqualTo("Header: one");
        assertThat(new String(body, StandardCharsets.UTF_8)).isEqualTo("HELLO");
        assertThat(secondLine).isEqualTo("Trailer: two");
    }

    @Test
    void testReadBodyReadsRawBytesNotCharacters() throws Exception {
        // given — "héllo" is 5 chars but 6 bytes in UTF-8
        var bytes = "héllo".getBytes(StandardCharsets.UTF_8);
        var reader = readerFor(bytes);
        // when
        var body = reader.readBody(bytes.length);
        // then
        assertThat(body).hasSize(6);
        assertThat(new String(body, StandardCharsets.UTF_8)).isEqualTo("héllo");
    }

    @Test
    void testReadBodyThrowsOnPrematureEndOfStream() {
        // given
        var reader = readerFor("abc");
        // when
        var thrown = catchThrowable(() -> reader.readBody(10));
        // then
        assertThat(thrown).isInstanceOf(ProtocolException.class);
    }
}

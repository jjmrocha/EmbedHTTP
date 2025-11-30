package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.api.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

class RequestParserTest {
    private final RequestParser classUnderTest = new RequestParser();

    @Test
    void testParseValidRequestWithoutBody() throws Exception {
        // given
        var rawRequest = """
                GET /test HTTP/1.1\r
                Host: localhost\r
                User-Agent: TestClient\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.getUrl()).isEqualTo("/test");
        assertThat(result.getHeaders()).containsEntry("Host", "localhost");
        assertThat(result.getHeaders()).containsEntry("User-Agent", "TestClient");
        assertThat(result.getBody()).isEmpty();
        assertThat(result.isKeepAlive()).isTrue();
    }

    @Test
    void testParseValidRequestWithBody() throws Exception {
        // given
        var rawRequest = """
                POST /submit HTTP/1.1\r
                Host: localhost\r
                Content-Length: 11\r
                \r
                Hello World""";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(result.getUrl()).isEqualTo("/submit");
        assertThat(result.getHeaders()).containsEntry("Host", "localhost");
        assertThat(result.getHeaders()).containsEntry("Content-Length", "11");
        assertThat(result.getBody()).isEqualTo("Hello World");
        assertThat(result.isKeepAlive()).isTrue();
    }

    @Test
    void testParseChunkedRequestBody() throws Exception {
        // given
        var rawRequest = """
                POST /upload HTTP/1.1\r
                Host: localhost\r
                Transfer-Encoding: chunked\r
                \r
                5\r
                Hello\r
                6\r
                 World\r
                0\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(result.getUrl()).isEqualTo("/upload");
        assertThat(result.getHeaders()).containsEntry("Host", "localhost");
        assertThat(result.getHeaders()).containsEntry("Transfer-Encoding", "chunked");
        assertThat(result.getBody()).isEqualTo("Hello World");
        assertThat(result.isKeepAlive()).isTrue();
    }

    @Test
    void testKeepAliveHeader() throws Exception {
        // given
        var rawRequest = """
                GET /test HTTP/1.1\r
                Host: localhost\r
                Connection: keep-alive\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.getUrl()).isEqualTo("/test");
        assertThat(result.getHeaders()).containsEntry("Host", "localhost");
        assertThat(result.getHeaders()).containsEntry("Connection", "keep-alive");
        assertThat(result.getBody()).isEmpty();
        assertThat(result.isKeepAlive()).isTrue();
    }

    @Test
    void testCloseConnection() throws IOException {
        // given
        var rawRequest = """
                GET /test HTTP/1.1\r
                Host: localhost\r
                Connection: close\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.getUrl()).isEqualTo("/test");
        assertThat(result.getHeaders()).containsEntry("Host", "localhost");
        assertThat(result.getHeaders()).containsEntry("Connection", "close");
        assertThat(result.getBody()).isEmpty();
        assertThat(result.isKeepAlive()).isFalse();
    }

    @Test
    void testParseInvalidRequestLine() {
        // given
        var rawRequest = "INVALID_REQUEST\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
    }

    @Test
    void testParseInvalidHeader() {
        // given
        var rawRequest = """
                GET /test HTTP/1.1\r
                InvalidHeader\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
    }

    @Test
    void testRejectRequestBodyTooLarge() {
        // given
        var contentLength = 11 * 1024 * 1024; // 11MB
        var rawRequest = "POST /upload HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Request body too large");
        assertThat(result).hasMessageContaining(String.valueOf(contentLength));
    }

    @Test
    void testAcceptRequestBodyAtMaxSize() throws Exception {
        // given
        var contentLength = 10 * 1024 * 1024; // 10MB
        var body = "X".repeat(contentLength);
        var rawRequest = "POST /upload HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "\r\n" +
                body;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(result.getBody()).hasSize(contentLength);
    }

    @Test
    void testRejectChunkedBodyChunkTooLarge() {
        // given
        var chunkSize = 2 * 1024 * 1024; // 2MB
        var hexChunkSize = Integer.toHexString(chunkSize);
        var rawRequest = "POST /upload HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                hexChunkSize + "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Chunk size too large");
        assertThat(result).hasMessageContaining(String.valueOf(chunkSize));
    }

    @Test
    void testAcceptChunkedBodyChunkAtMaxSize() throws Exception {
        // given
        var chunkSize = 1024 * 1024; // 1MB
        var hexChunkSize = Integer.toHexString(chunkSize);
        var body = "X".repeat(chunkSize);
        var rawRequest = "POST /upload HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                hexChunkSize + "\r\n" +
                body + "\r\n" +
                "0\r\n" +
                "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(result.getBody()).hasSize(chunkSize);
    }
}
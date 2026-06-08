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
    void testParseBodyByByteCountNotCharCount() throws Exception {
        // given — "héllo" is 5 characters but 6 bytes in UTF-8; Content-Length counts octets
        var body = "héllo";
        var contentLength = body.getBytes(StandardCharsets.UTF_8).length; // 6
        var rawRequest = "POST /submit HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "\r\n" +
                body;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getBody()).isEqualTo("héllo");
    }

    @Test
    void testParseTwoRequestsOnSameReaderWithoutOverReading() throws Exception {
        // given — two back-to-back requests on one connection; the first has a body
        var rawRequests = "POST /first HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: 5\r\n" +
                "\r\n" +
                "HELLO" +
                "GET /second HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";
        var reader = new HttpConnectionReader(
                new ByteArrayInputStream(rawRequests.getBytes(StandardCharsets.UTF_8))
        );
        // when
        var first = classUnderTest.parseRequest(reader);
        var second = classUnderTest.parseRequest(reader);
        // then
        assertThat(first.getUrl()).isEqualTo("/first");
        assertThat(first.getBody()).isEqualTo("HELLO");
        assertThat(second.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(second.getUrl()).isEqualTo("/second");
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

    @Test
    void testRejectTooManyHeaders() {
        // given
        var builder = new StringBuilder();
        builder.append("GET /test HTTP/1.1\r\n");
        for (int i = 0; i < 101; i++) { // 101 headers exceeds limit of 100
            builder.append("Header-").append(i).append(": value\r\n");
        }
        builder.append("\r\n");
        var inputStream = new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Too many headers");
        assertThat(result).hasMessageContaining("100");
    }

    @Test
    void testAcceptMaxHeaderCount() throws Exception {
        // given
        var builder = new StringBuilder();
        builder.append("GET /test HTTP/1.0\r\n"); // 1.0 so the Host requirement does not apply; header limits are version-independent
        for (int i = 0; i < 100; i++) { // Exactly 100 headers at the limit
            builder.append("Header-").append(i).append(": value\r\n");
        }
        builder.append("\r\n");
        var inputStream = new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.getHeaders()).hasSize(100);
    }

    @Test
    void testRejectHeaderTooLarge() {
        // given
        var headerValue = "X".repeat(8193); // 8193 bytes exceeds 8KB limit
        var rawRequest = "GET /test HTTP/1.1\r\n" +
                "Large-Header: " + headerValue + "\r\n" +
                "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("maximum length");
        assertThat(result).hasMessageContaining("8192");
    }

    @Test
    void testAcceptHeaderAtMaxSize() throws Exception {
        // given
        var headerValue = "X".repeat(8178); // Total header line is exactly 8192 bytes
        var rawRequest = "GET /test HTTP/1.0\r\n" + // 1.0 so the Host requirement does not apply; header limits are version-independent
                "Large-Header: " + headerValue + "\r\n" +
                "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.getHeaders()).containsKey("Large-Header");
    }

    @Test
    void testAcceptHttp10Version() throws Exception {
        // given
        var rawRequest = """
                GET /test HTTP/1.0\r
                Host: localhost\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.getUrl()).isEqualTo("/test");
    }

    @Test
    void testAcceptHttp11Version() throws Exception {
        // given
        var rawRequest = """
                GET /test HTTP/1.1\r
                Host: localhost\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.getUrl()).isEqualTo("/test");
    }

    @Test
    void testRejectHttp20Version() {
        // given
        var rawRequest = """
                GET /test HTTP/2.0\r
                Host: localhost\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Unsupported HTTP version");
        assertThat(result).hasMessageContaining("HTTP/2.0");
    }

    @Test
    void testRejectHttp30Version() {
        // given
        var rawRequest = """
                GET /test HTTP/3.0\r
                Host: localhost\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Unsupported HTTP version");
        assertThat(result).hasMessageContaining("HTTP/3.0");
    }

    @Test
    void testRejectInvalidVersionFormat() {
        // given
        var rawRequest = """
                GET /test GARBAGE\r
                Host: localhost\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Unsupported HTTP version");
        assertThat(result).hasMessageContaining("GARBAGE");
    }

    @Test
    void testRejectEmptyVersion() {
        // given
        var rawRequest = """
                GET /test \r
                Host: localhost\r
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
    void testRejectHttpVersionWithExtraSpaces() {
        // given
        var rawRequest = """
                GET /test HTTP /1.1\r
                Host: localhost\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Unsupported HTTP version");
    }

    @Test
    void testRejectLowercaseHttpVersion() {
        // given
        var rawRequest = """
                GET /test http/1.1\r
                Host: localhost\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() ->
                classUnderTest.parseRequest(inputStream)
        );
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Unsupported HTTP version");
        assertThat(result).hasMessageContaining("http/1.1");
    }

    // --- M3: protocol hardening ---

    @Test
    void testRejectNonNumericContentLength() {
        // given
        var rawRequest = "POST /submit HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: abc\r\n" +
                "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() -> classUnderTest.parseRequest(inputStream));
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Content-Length");
    }

    @Test
    void testRejectNegativeContentLength() {
        // given
        var rawRequest = "POST /submit HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: -5\r\n" +
                "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() -> classUnderTest.parseRequest(inputStream));
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Content-Length");
    }

    @Test
    void testRejectInvalidChunkSize() {
        // given
        var rawRequest = "POST /upload HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "xyz\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() -> classUnderTest.parseRequest(inputStream));
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("chunk size");
    }

    @Test
    void testParseChunkedBodyWithChunkExtension() throws Exception {
        // given — a chunk-size line may carry extensions: "5;name=value"
        var rawRequest = "POST /upload HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "5;name=value\r\n" +
                "Hello\r\n" +
                "0\r\n" +
                "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.getBody()).isEqualTo("Hello");
    }

    @Test
    void testRejectChunkedBodyExceedingMaxAggregateSize() {
        // given — ten 1MB chunks reach the 10MB cap; the eleventh must be rejected before reading
        var oneMegabyte = "X".repeat(1024 * 1024);
        var builder = new StringBuilder();
        builder.append("POST /upload HTTP/1.1\r\n")
                .append("Host: localhost\r\n")
                .append("Transfer-Encoding: chunked\r\n")
                .append("\r\n");
        for (int i = 0; i < 10; i++) {
            builder.append("100000\r\n").append(oneMegabyte).append("\r\n");
        }
        builder.append("100000\r\n"); // eleventh chunk size — would push past 10MB
        var inputStream = new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() -> classUnderTest.parseRequest(inputStream));
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("too large");
    }

    @Test
    void testRejectContentLengthAndTransferEncoding() {
        // given — both framing headers present is a request-smuggling vector (RFC 9112 §6.1)
        var rawRequest = "POST /submit HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: 5\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "HELLO";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() -> classUnderTest.parseRequest(inputStream));
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
    }

    @Test
    void testHttp10DefaultsToCloseWithoutConnectionHeader() throws Exception {
        // given
        var rawRequest = """
                GET /test HTTP/1.0\r
                Host: localhost\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.isKeepAlive()).isFalse();
    }

    @Test
    void testHttp10KeepAliveWithExplicitHeader() throws Exception {
        // given
        var rawRequest = """
                GET /test HTTP/1.0\r
                Host: localhost\r
                Connection: keep-alive\r
                \r
                """;
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = classUnderTest.parseRequest(inputStream);
        // then
        assertThat(result.isKeepAlive()).isTrue();
    }

    @Test
    void testRejectHttp11RequestWithoutHost() {
        // given
        var rawRequest = "GET /test HTTP/1.1\r\n\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() -> classUnderTest.parseRequest(inputStream));
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Host");
    }

    @Test
    void testRejectDuplicateHostHeader() {
        // given
        var rawRequest = "GET /test HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Host: evil.example\r\n" +
                "\r\n";
        var inputStream = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        // when
        var result = catchThrowable(() -> classUnderTest.parseRequest(inputStream));
        // then
        assertThat(result).isInstanceOf(ProtocolException.class);
        assertThat(result).hasMessageContaining("Host");
    }
}
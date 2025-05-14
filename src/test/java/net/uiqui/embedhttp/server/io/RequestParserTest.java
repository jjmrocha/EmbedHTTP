package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.api.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
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
}
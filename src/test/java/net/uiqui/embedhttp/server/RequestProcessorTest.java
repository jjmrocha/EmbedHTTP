package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.impl.RouterImpl;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RequestProcessorTest {
    private final RequestParser requestParser = new RequestParser();
    private final ResponseWriter responseWriter = new ResponseWriter();
    private final Router router = Router.newRouter()
            .put("/error", request -> {
                throw new RuntimeException("Error");
            })
            .get("/test", request -> HttpResponse.ok()
                    .setBody(ContentType.TEXT_PLAIN, "Hello World")
            );
    private final RequestProcessor classUnderTest = new RequestProcessor(
            requestParser,
            responseWriter,
            (RouterImpl) router
    );

    @Test
    void testProcessValidRequest() throws IOException {
        // given
        var inputStream = buildInputStream(
                """
                        GET /test HTTP/1.1\r
                        Host: localhost\r
                        User-Agent: TestClient\r
                        \r
                        """
        );
        var outputStream = new ByteArrayOutputStream();
        var clientSocket = buildClientSocket(inputStream, outputStream);
        // when
        classUnderTest.process(clientSocket);
        // then
        var expected = """
                HTTP/1.1 200 OK\r
                Content-Length: 11\r
                Content-Type: text/plain\r
                Connection: close\r
                \r
                Hello World""";
        var result = outputStream.toString();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testProcessWithInvalidRequest() throws IOException {
        // given
        var inputStream = buildInputStream(
                """
                        POST /noContent\r
                        """
        );
        var outputStream = new ByteArrayOutputStream();
        var clientSocket = buildClientSocket(inputStream, outputStream);
        // when
        classUnderTest.process(clientSocket);
        // then
        var expected = """
                HTTP/1.1 400 Bad Request\r
                Content-Length: 50\r
                Content-Type: text/plain\r  
                Connection: close\r
                \r
                Bad Request: Invalid request line: POST /noContent""";
        var result = outputStream.toString();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testProcessWithNotFoundRequest() throws IOException {
        // given
        var inputStream = buildInputStream(
                """
                        GET /notFound HTTP/1.1\r
                        Host: localhost\r
                        User-Agent: TestClient\r
                        \r
                        """
        );
        var outputStream = new ByteArrayOutputStream();
        var clientSocket = buildClientSocket(inputStream, outputStream);
        // when
        classUnderTest.process(clientSocket);
        // then
        var expected = """
                HTTP/1.1 404 Not Found\r
                Content-Length: 19\r
                Content-Type: text/plain\r
                Connection: close\r
                \r
                Not Found:/notFound""";
        var result = outputStream.toString();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testProcessWithError() throws IOException {
        // given
        var inputStream = buildInputStream(
                """
                        PUT /error HTTP/1.1\r
                        Host: localhost\r
                        User-Agent: TestClient\r
                        \r
                        """
        );
        var outputStream = new ByteArrayOutputStream();
        var clientSocket = buildClientSocket(inputStream, outputStream);
        // when
        classUnderTest.process(clientSocket);
        // then
        var expected = """
                HTTP/1.1 500 Internal Server Error\r
                Content-Length: 34\r
                Content-Type: text/plain\r
                Connection: close\r
                \r
                Unexpected error executing request""";
        var result = outputStream.toString();
        assertThat(result).isEqualTo(expected);
    }

    private static Socket buildClientSocket(InputStream inputStream, ByteArrayOutputStream outputStream) throws IOException {
        var clientSocket = mock(Socket.class);
        given(clientSocket.getInputStream()).willReturn(inputStream);
        given(clientSocket.getOutputStream()).willReturn(outputStream);
        return clientSocket;
    }

    private static InputStream buildInputStream(String rawRequest) {
        return new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
    }
}
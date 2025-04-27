package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.ContentType;
import net.uiqui.embedhttp.api.HttpStatusCode;
import net.uiqui.embedhttp.api.impl.HttpResponseImpl;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseWriterTest {

    @Test
    void testWriteResponseWithoutBody() throws IOException {
        // given
        var response = buildResponse(HttpStatusCode.NO_CONTENT, null);
        var outputStream = new ByteArrayOutputStream();
        // when
        ResponseWriter.writeResponse(outputStream, response);
        // then
        var expected = """
                HTTP/1.1 204 No Content\r
                Connection: close\r
                \r
                """;
        var result = outputStream.toString();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testWriteResponseWithBody() throws IOException {
        // given
        var response = buildResponse(HttpStatusCode.OK, "Hello World");
        var outputStream = new ByteArrayOutputStream();
        // when
        ResponseWriter.writeResponse(outputStream, response);
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

    private HttpResponseImpl buildResponse(HttpStatusCode status, String body) {
        var response = new HttpResponseImpl();
        response.setStatus(status);

        if (body != null) {
            response.setBody(ContentType.TEXT_PLAIN, body);
        }

        return response;
    }
}
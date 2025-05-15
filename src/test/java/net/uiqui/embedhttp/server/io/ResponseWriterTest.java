package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpStatusCode;
import net.uiqui.embedhttp.api.impl.HttpResponseImpl;
import net.uiqui.embedhttp.server.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class ResponseWriterTest {
    private final ResponseWriter classUnderTest = new ResponseWriter();

    private MockedStatic<Now> mockedNow;

    @BeforeEach
    void setUp() {
        mockedNow = mockStatic(Now.class);
        var now = ZonedDateTime.parse("2023-10-01T12:00:00.100Z");
        mockedNow.when(Now::asZonedDateTime).thenReturn(now);
    }

    @AfterEach
    void tearDown() {
        mockedNow.close();
    }

    @Test
    void testWriteResponseWithoutBody() throws IOException {
        // given
        var response = buildResponse(HttpStatusCode.NO_CONTENT, null);
        var outputStream = new ByteArrayOutputStream();
        // when
        classUnderTest.writeResponse(outputStream, response);
        // then
        var expected = """
                HTTP/1.1 204 No Content\r
                Date: Sun, 01 Oct 2023 12:00:00 GMT\r
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
        classUnderTest.writeResponse(outputStream, response);
        // then
        var expected = """
                HTTP/1.1 200 OK\r
                Content-Length: 11\r
                Content-Type: text/plain\r
                Date: Sun, 01 Oct 2023 12:00:00 GMT\r
                \r
                Hello World""";
        var result = outputStream.toString();
        assertThat(result).isEqualTo(expected);
    }

    private HttpResponseImpl buildResponse(HttpStatusCode status, String body) {
        var response = new HttpResponseImpl(status);

        if (body != null) {
            response.setBody(ContentType.TEXT_PLAIN, body);
        }

        return response;
    }
}
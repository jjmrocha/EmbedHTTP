package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpStatusCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseImplTest {

    @ParameterizedTest
    @MethodSource("statusCodes")
    void testSetStatusWithHttpStatusCode(HttpStatusCode statusCode, int code, String message) {
        // given
        var classUnderTest = new HttpResponseImpl();
        // when
        classUnderTest.setStatus(statusCode);
        // then
        assertThat(classUnderTest.getStatusCode()).isEqualTo(code);
        assertThat(classUnderTest.getStatusMessage()).isEqualTo(message);
    }

    @Test
    void testSetStatus() {
        // given
        var classUnderTest = new HttpResponseImpl();
        var statusCode = 404;
        var statusMessage = "Not Found";
        // when
        classUnderTest.setStatus(statusCode, statusMessage);
        // then
        assertThat(classUnderTest.getStatusCode()).isEqualTo(404);
        assertThat(classUnderTest.getStatusMessage()).isEqualTo("Not Found");
    }

    @ParameterizedTest
    @MethodSource("herders")
    void testSetHeaderWithHttpHeader(HttpHeader header, String value) {
        // given
        var classUnderTest = new HttpResponseImpl();
        // when
        classUnderTest.setHeader(header, value + " value");
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(header.getValue(), value + " value");
    }

    @Test
    void testSetHeader() {
        // given
        var classUnderTest = new HttpResponseImpl();
        var headerName = "X-Custom-Header";
        var headerValue = "CustomValue";
        // when
        classUnderTest.setHeader(headerName, headerValue);
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(headerName, headerValue);
    }

    @Test
    void testSetContentType() {
        // given
        var classUnderTest = new HttpResponseImpl();
        var contentType = "application/json";
        // when
        classUnderTest.setContentType(contentType);
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), contentType);
    }

    @Test
    void testSetBody() {
        // given
        var classUnderTest = new HttpResponseImpl();
        var body = "Hello, World!";
        // when
        classUnderTest.setBody(body);
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), String.valueOf(body.length()));
        assertThat(classUnderTest.getBody()).isEqualTo(body);
    }

    private static Stream<Arguments> statusCodes() {
        return Arrays.stream(HttpStatusCode.values())
                .map(statusCode -> Arguments.of(statusCode, statusCode.getCode(), statusCode.getReasonPhrase()));
    }

    private static Stream<Arguments> herders() {
        return Arrays.stream(HttpHeader.values())
                .map(header -> Arguments.of(header, header.getValue()));
    }
}
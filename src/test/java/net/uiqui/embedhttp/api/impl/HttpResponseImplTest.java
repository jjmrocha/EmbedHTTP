package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.api.ContentType;
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
        // when
        var classUnderTest = new HttpResponseImpl(statusCode);
        // then
        assertThat(classUnderTest.getStatusCode()).isEqualTo(code);
        assertThat(classUnderTest.getStatusMessage()).isEqualTo(message);
    }

    @Test
    void testSetStatus() {
        // given
        var statusCode = 404;
        var statusMessage = "Not Found";
        // when
        var classUnderTest = new HttpResponseImpl(statusCode, statusMessage);
        // then
        assertThat(classUnderTest.getStatusCode()).isEqualTo(404);
        assertThat(classUnderTest.getStatusMessage()).isEqualTo("Not Found");
    }

    @ParameterizedTest
    @MethodSource("herders")
    void testSetHeaderWithHttpHeader(HttpHeader header, String value) {
        // given
        var classUnderTest = new HttpResponseImpl(HttpStatusCode.OK);
        // when
        classUnderTest.setHeader(header, value + " value");
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(header.getValue(), value + " value");
    }

    @Test
    void testSetHeader() {
        // given
        var classUnderTest = new HttpResponseImpl(HttpStatusCode.OK);
        var headerName = "X-Custom-Header";
        var headerValue = "CustomValue";
        // when
        classUnderTest.setHeader(headerName, headerValue);
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(headerName, headerValue);
    }

    @Test
    void testSetBodyWithContentType() {
        // given
        var classUnderTest = new HttpResponseImpl(HttpStatusCode.OK);
        var contentType = ContentType.APPLICATION_JSON;
        var body = "{\"key\": \"value\"}";
        // when
        classUnderTest.setBody(contentType, body);
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), contentType.getValue());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), "16");
        assertThat(classUnderTest.getBody()).isEqualTo(body);
    }

    @Test
    void testSetBody() {
        // given
        var classUnderTest = new HttpResponseImpl(HttpStatusCode.OK);
        var contentType = "application/json";
        var body = "{\"key\": \"value\"}";
        // when
        classUnderTest.setBody(contentType, body);
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), contentType);
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), "16");
        assertThat(classUnderTest.getBody()).isEqualTo(body);
    }

    @Test
    void testSetBodyWithUTF8Characters() {
        // given
        var classUnderTest = new HttpResponseImpl(HttpStatusCode.OK);
        var contentType = ContentType.TEXT_PLAIN;
        
        // Body: "Hello 世界" = 8 characters, but 13 bytes in UTF-8
        // "Hello " = 6 bytes
        // "世" = 3 bytes (U+4E16)
        // "界" = 3 bytes (U+754C)
        // Total = 6 + 3 + 3 = 12 bytes
        var body = "Hello 世界";
        
        // when
        classUnderTest.setBody(contentType, body);
        
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), contentType.getValue());
        // Content-Length should be 12 bytes, not 8 characters
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), "12");
        assertThat(classUnderTest.getBody()).isEqualTo(body);
    }

    @Test
    void testSetBodyWithVariousUTF8Characters() {
        // given
        var classUnderTest = new HttpResponseImpl(HttpStatusCode.OK);
        var contentType = "application/json";
        
        // Test with emojis and special characters
        // "café 🎉" = 7 characters
        // "café " = "caf" (3) + "é" (2 bytes: C3 A9) + " " (1) = 6 bytes
        // "🎉" = 4 bytes (F0 9F 8E 89)
        // Total = 6 + 4 = 10 bytes
        var body = "café 🎉";
        
        // when
        classUnderTest.setBody(contentType, body);
        
        // then
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), contentType);
        // Content-Length should be 10 bytes, not 7 characters
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), "10");
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
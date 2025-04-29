package net.uiqui.embedhttp.api;

import net.uiqui.embedhttp.api.impl.HttpResponseImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseTest {

    @Test
    void testOk() {
        // when
        var result = HttpResponse.ok(ContentType.TEXT_PLAIN, "Hello, World!");
        // then
        assertThat(result).isInstanceOf(HttpResponseImpl.class);
        var classUnderTest = (HttpResponseImpl) result;
        assertThat(classUnderTest.getStatusCode()).isEqualTo(HttpStatusCode.OK.getCode());
        assertThat(classUnderTest.getStatusMessage()).isEqualTo(HttpStatusCode.OK.getReasonPhrase());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), ContentType.TEXT_PLAIN.getValue());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), "13");
        assertThat(classUnderTest.getBody()).isEqualTo("Hello, World!");
    }

    @Test
    void testNoContent() {
        // when
        var result = HttpResponse.noContent();
        // then
        assertThat(result).isInstanceOf(HttpResponseImpl.class);
        var classUnderTest = (HttpResponseImpl) result;
        assertThat(classUnderTest.getStatusCode()).isEqualTo(HttpStatusCode.NO_CONTENT.getCode());
        assertThat(classUnderTest.getStatusMessage()).isEqualTo(HttpStatusCode.NO_CONTENT.getReasonPhrase());
        assertThat(classUnderTest.getHeaders()).isEmpty();
        assertThat(classUnderTest.getBody()).isNull();
    }

    @Test
    void testNotFound() {
        // when
        var result = HttpResponse.notFound(ContentType.TEXT_PLAIN, "Resource not found");
        // then
        assertThat(result).isInstanceOf(HttpResponseImpl.class);
        var classUnderTest = (HttpResponseImpl) result;
        assertThat(classUnderTest.getStatusCode()).isEqualTo(HttpStatusCode.NOT_FOUND.getCode());
        assertThat(classUnderTest.getStatusMessage()).isEqualTo(HttpStatusCode.NOT_FOUND.getReasonPhrase());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), ContentType.TEXT_PLAIN.getValue());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), "18");
        assertThat(classUnderTest.getBody()).isEqualTo("Resource not found");
    }

    @Test
    void testBadRequest() {
        // when
        var result = HttpResponse.badRequest(ContentType.APPLICATION_JSON, "{\"error\": \"Bad Request\"}");
        // then
        assertThat(result).isInstanceOf(HttpResponseImpl.class);
        var classUnderTest = (HttpResponseImpl) result;
        assertThat(classUnderTest.getStatusCode()).isEqualTo(HttpStatusCode.BAD_REQUEST.getCode());
        assertThat(classUnderTest.getStatusMessage()).isEqualTo(HttpStatusCode.BAD_REQUEST.getReasonPhrase());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), ContentType.APPLICATION_JSON.getValue());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), "24");
        assertThat(classUnderTest.getBody()).isEqualTo("{\"error\": \"Bad Request\"}");
    }

    @Test
    void testUnexpectedError() {
        // when
        var result = HttpResponse.unexpectedError(ContentType.TEXT_PLAIN, "An unexpected error occurred");
        // then
        assertThat(result).isInstanceOf(HttpResponseImpl.class);
        var classUnderTest = (HttpResponseImpl) result;
        assertThat(classUnderTest.getStatusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode());
        assertThat(classUnderTest.getStatusMessage()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_TYPE.getValue(), ContentType.TEXT_PLAIN.getValue());
        assertThat(classUnderTest.getHeaders()).containsEntry(HttpHeader.CONTENT_LENGTH.getValue(), "28");
        assertThat(classUnderTest.getBody()).isEqualTo("An unexpected error occurred");
    }
}
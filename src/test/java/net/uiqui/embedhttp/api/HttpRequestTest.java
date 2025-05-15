package net.uiqui.embedhttp.api;

import net.uiqui.embedhttp.api.impl.HttpRequestImpl;
import net.uiqui.embedhttp.server.InsensitiveMap;
import net.uiqui.embedhttp.server.Request;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {
    @Test
    void testGetPathParameterWhenParameterExists() {
        // given
        var pathParameters = Map.of("id", "123");
        var classUnderTest = new HttpRequestImpl(null, null, pathParameters);
        // when
        var result = classUnderTest.getPathParameter("id");
        // then
        assertThat(result).isEqualTo("123");
    }

    @Test
    void testGetPathParameterWhenParameterDoNotExists() {
        // given
        var pathParameters = Map.of("id", "123");
        var classUnderTest = new HttpRequestImpl(null, null, pathParameters);
        // when
        var result = classUnderTest.getPathParameter("other");
        // then
        assertThat(result).isNull();
    }

    @Test
    void testGetQueryParameterWhenParameterExists() {
        // given
        var request = new Request(HttpMethod.GET, "/resource?name=test", null, null, false);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getQueryParameter("name");
        // then
        assertThat(result).isEqualTo("test");
    }

    @Test
    void testGetQueryParameterWhenParameterDoNotExists() {
        // given
        var request = new Request(HttpMethod.GET, "/resource?name=test", null, null, false);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getQueryParameter("other");
        // then
        assertThat(result).isNull();
    }

    @Test
    void testGetHeaderWhenHeaderExistsInLowerCase() {
        // given
        var headers = InsensitiveMap.from(Map.of("header", "value"));
        var request = new Request(HttpMethod.GET, "/resource", headers, null, false);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getHeader("header");
        // then
        assertThat(result).isEqualTo("value");
    }

    @Test
    void testGetHeaderWhenHeaderExistsInUpperCase() {
        // given
        var headers = InsensitiveMap.from(Map.of("header", "value"));
        var request = new Request(HttpMethod.GET, "/resource", headers, null, false);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getHeader("HEADER");
        // then
        assertThat(result).isEqualTo("value");
    }

    @Test
    void testGetHeaderWhenHeaderDoNotExists() {
        // given
        var headers = InsensitiveMap.from(Map.of("header", "value"));
        var request = new Request(HttpMethod.GET, "/resource", headers, null, false);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getHeader("other");
        // then
        assertThat(result).isNull();
    }

    @Test
    void testGetContentType() {
        // given
        var headers = InsensitiveMap.from(Map.of(HttpHeader.CONTENT_TYPE.getValue(), "application/json"));
        var request = new Request(HttpMethod.GET, "/resource", headers, null, false);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getContentType();
        // then
        assertThat(result).isEqualTo("application/json");
    }

    @Test
    void testGetAccept() {
        // given
        var headers = InsensitiveMap.from(Map.of(HttpHeader.ACCEPT.getValue(), "application/json"));
        var request = new Request(HttpMethod.GET, "/resource", headers, null, false);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getAccept();
        // then
        assertThat(result).isEqualTo("application/json");
    }
}
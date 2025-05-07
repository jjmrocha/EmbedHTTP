package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.routing.Route;
import net.uiqui.embedhttp.server.InsensitiveMap;
import net.uiqui.embedhttp.server.Request;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestImplTest {
    @Test
    void testGetMethod() {
        // given
        var request = new Request(HttpMethod.GET, "/", null, null);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getMethod();
        // then
        assertThat(result).isEqualTo(HttpMethod.GET);
    }

    @Test
    void testGetURL() {
        // given
        var request = new Request(HttpMethod.GET, "/test", null, null);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getURL();
        // then
        assertThat(result).isEqualTo("/test");
    }

    @Test
    void testGetPath() {
        // given
        var request = new Request(HttpMethod.GET, "/test/path?name=value", null, null);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getPath();
        // then
        assertThat(result).isEqualTo("/test/path");
    }

    @Test
    void testGetQueryParameters() {
        // given
        var request = new Request(HttpMethod.GET, "/test/path?name1=value1&name2=val+2", null, null);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getQueryParameters();
        // then
        assertThat(result)
                .containsEntry("name1", "value1")
                .containsEntry("name2", "val 2");
    }

    @Test
    void testGetQueryParametersWithEmojis() {
        // given
        var request = new Request(HttpMethod.GET, "/test/path?q=%F0%9F%92%A1", null, null);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getQueryParameters();
        // then
        assertThat(result).containsEntry("q", "\uD83D\uDCA1");
    }

    @Test
    void testGetQueryParametersWhenURLHasNoQuery() {
        // given
        var request = new Request(HttpMethod.GET, "/test/path", null, null);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getQueryParameters();
        // then
        assertThat(result).isEmpty();
    }

    @Test
    void testGetHeaders() {
        // given
        var headers = InsensitiveMap.from(Map.of("header1", "value1", "header2", "value2"));
        var request = new Request(HttpMethod.GET, "/", headers, null);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getHeaders();
        // then
        assertThat(result).isEqualTo(headers);
    }

    @Test
    void testGetBody() {
        // given
        var body = "This is a test body";
        var request = new Request(HttpMethod.POST, "/", null, body);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.getBody();
        // then
        assertThat(result).isEqualTo(body);
    }

    @Test
    void testRequest() {
        // given
        var request = new Request(HttpMethod.GET, "/", null, null);
        var classUnderTest = new HttpRequestImpl(request, null, null);
        // when
        var result = classUnderTest.request();
        // then
        assertThat(result).isEqualTo(request);
    }

    @Test
    void testRoute() {
        // given
        var route = new Route(HttpMethod.GET, "/", null);
        var classUnderTest = new HttpRequestImpl(null, route, null);
        // when
        var result = classUnderTest.route();
        // then
        assertThat(result).isEqualTo(route);
    }

    @Test
    void testGetPathParameters() {
        // given
        var pathParameters = InsensitiveMap.from(Map.of("param1", "value1", "param2", "value2"));
        var classUnderTest = new HttpRequestImpl(null, null, pathParameters);
        // when
        var result = classUnderTest.getPathParameters();
        // then
        assertThat(result).isEqualTo(pathParameters);
    }
}
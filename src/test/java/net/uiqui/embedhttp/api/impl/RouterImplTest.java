package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.server.Request;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouterImplTest {
    @Test
    void testRouteRequestWithGetMethod() {
        // given
        var classUnderTest = buildRouterImpl();
        var request = new Request(HttpMethod.GET, "/get?name=value", null, null);
        // when
        var result = classUnderTest.routeRequest(request);
        // then
        assertThat(result).isNotNull();
        assertThat(result.route().getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.route().getPathPattern()).isEqualTo("/get");
        assertThat(result.request()).isEqualTo(request);
    }

    @Test
    void testRouteRequestWithPutMethodAndPathParameter() {
        // given
        var classUnderTest = buildRouterImpl();
        var request = new Request(HttpMethod.PUT, "/put/123", null, null);
        // when
        var result = classUnderTest.routeRequest(request);
        // then
        assertThat(result).isNotNull();
        assertThat(result.route().getMethod()).isEqualTo(HttpMethod.PUT);
        assertThat(result.route().getPathPattern()).isEqualTo("/put/:id");
        assertThat(result.request()).isEqualTo(request);
        assertThat(result.getPathParameters()).containsEntry("id", "123");
    }

    @Test
    void testRouteRequestWithPostMethod() {
        // given
        var classUnderTest = buildRouterImpl();
        var request = new Request(HttpMethod.POST, "/post", null, null);
        // when
        var result = classUnderTest.routeRequest(request);
        // then
        assertThat(result).isNull();
    }

    @Test
    void testRouteRequestWithPutMethodAndNoPathParameter() {
        // given
        var classUnderTest = buildRouterImpl();
        var request = new Request(HttpMethod.PUT, "/put", null, null);
        // when
        var result = classUnderTest.routeRequest(request);
        // then
        assertThat(result).isNull();
    }

    @Test
    void testRouteRequestWithMultiplePathParameters() {
        // given
        var classUnderTest = buildRouterImpl();
        var request = new Request(HttpMethod.POST, "/v1/resource/123/section/abc", null, null);
        // when
        var result = classUnderTest.routeRequest(request);
        // then
        assertThat(result).isNotNull();
        assertThat(result.route().getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(result.route().getPathPattern()).isEqualTo("/v1/resource/:id/section/:name");
        assertThat(result.request()).isEqualTo(request);
        assertThat(result.getPathParameters()).containsEntry("id", "123");
        assertThat(result.getPathParameters()).containsEntry("name", "abc");
    }

    private static RouterImpl buildRouterImpl() {
        HttpRequestHandler handler = x -> HttpResponse.noContent();
        var router = Router.newRouter()
                .get("/get", handler)
                .put("/put/:id", handler)
                .post("/v1/resource/:id/section/:name", handler);

        assertThat(router).isInstanceOf(RouterImpl.class);
        return (RouterImpl) router;
    }
}
package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.routing.Route;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingBuilderTest {
    @Test
    void testNewRouter() {
        // given
        HttpRequestHandler handler = request -> HttpResponse.noContent();
        // when
        var result = Router.newRouter()
                .get("/get", handler)
                .post("/post", handler)
                .put("/put", handler)
                .delete("/delete", handler)
                .head("/head", handler)
                .options("/options", handler)
                .patch("/patch", handler);
        // then
        assertThat(result).isInstanceOf(RoutingBuilder.class);
        var classUnderTest = (RoutingBuilder) result;
        assertThat(classUnderTest.getRoutesForMethod(HttpMethod.GET)).containsExactly(new Route(HttpMethod.GET, "/get", handler));
        assertThat(classUnderTest.getRoutesForMethod(HttpMethod.POST)).containsExactly(new Route(HttpMethod.POST, "/post", handler));
        assertThat(classUnderTest.getRoutesForMethod(HttpMethod.PUT)).containsExactly(new Route(HttpMethod.PUT, "/put", handler));
        assertThat(classUnderTest.getRoutesForMethod(HttpMethod.DELETE)).containsExactly(new Route(HttpMethod.DELETE, "/delete", handler));
        assertThat(classUnderTest.getRoutesForMethod(HttpMethod.HEAD)).containsExactly(new Route(HttpMethod.HEAD, "/head", handler));
        assertThat(classUnderTest.getRoutesForMethod(HttpMethod.OPTIONS)).containsExactly(new Route(HttpMethod.OPTIONS, "/options", handler));
        assertThat(classUnderTest.getRoutesForMethod(HttpMethod.PATCH)).containsExactly(new Route(HttpMethod.PATCH, "/patch", handler));
    }
}
package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.api.HttpResponse;
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
        assertThat(classUnderTest.getRouteTreeForMethod(HttpMethod.GET).getAllRoutes()).containsExactly(new Route(HttpMethod.GET, "/get", handler));
        assertThat(classUnderTest.getRouteTreeForMethod(HttpMethod.POST).getAllRoutes()).containsExactly(new Route(HttpMethod.POST, "/post", handler));
        assertThat(classUnderTest.getRouteTreeForMethod(HttpMethod.PUT).getAllRoutes()).containsExactly(new Route(HttpMethod.PUT, "/put", handler));
        assertThat(classUnderTest.getRouteTreeForMethod(HttpMethod.DELETE).getAllRoutes()).containsExactly(new Route(HttpMethod.DELETE, "/delete", handler));
        assertThat(classUnderTest.getRouteTreeForMethod(HttpMethod.HEAD).getAllRoutes()).containsExactly(new Route(HttpMethod.HEAD, "/head", handler));
        assertThat(classUnderTest.getRouteTreeForMethod(HttpMethod.OPTIONS).getAllRoutes()).containsExactly(new Route(HttpMethod.OPTIONS, "/options", handler));
        assertThat(classUnderTest.getRouteTreeForMethod(HttpMethod.PATCH).getAllRoutes()).containsExactly(new Route(HttpMethod.PATCH, "/patch", handler));
    }
}
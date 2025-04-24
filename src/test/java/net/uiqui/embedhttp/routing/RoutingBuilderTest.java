package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingBuilderTest {

    @Test
    void testNewRouter() {
        // given
        HttpRequestHandler handler = (request, response) -> {
        };
        // when
        var result = RoutingBuilder.newRouter()
                .get("/get", handler)
                .post("/post", handler)
                .put("/put", handler)
                .delete("/delete", handler)
                .head("/head", handler)
                .options("/options", handler)
                .patch("/patch", handler)
                .build();
        // then
        assertThat(result).isInstanceOf(Router.class);
        assertThat(result.getRoutes(HttpMethod.GET)).containsExactly(new Route(HttpMethod.GET, "/get", handler));
        assertThat(result.getRoutes(HttpMethod.POST)).containsExactly(new Route(HttpMethod.POST, "/post", handler));
        assertThat(result.getRoutes(HttpMethod.PUT)).containsExactly(new Route(HttpMethod.PUT, "/put", handler));
        assertThat(result.getRoutes(HttpMethod.DELETE)).containsExactly(new Route(HttpMethod.DELETE, "/delete", handler));
        assertThat(result.getRoutes(HttpMethod.HEAD)).containsExactly(new Route(HttpMethod.HEAD, "/head", handler));
        assertThat(result.getRoutes(HttpMethod.OPTIONS)).containsExactly(new Route(HttpMethod.OPTIONS, "/options", handler));
        assertThat(result.getRoutes(HttpMethod.PATCH)).containsExactly(new Route(HttpMethod.PATCH, "/patch", handler));
    }
}
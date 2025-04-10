package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequest;
import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.api.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RoutingBuilderTest {

    @Test
    void newRouter() {
        // given
        HttpRequestHandler handler = (request, response) -> {};
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
        assertThat(result.getRoutes(HttpMethod.GET)).contains(new Route(HttpMethod.GET, "/get", handler));
        assertThat(result.getRoutes(HttpMethod.POST)).contains(new Route(HttpMethod.POST, "/post", handler));
        assertThat(result.getRoutes(HttpMethod.PUT)).contains(new Route(HttpMethod.PUT, "/put", handler));
        assertThat(result.getRoutes(HttpMethod.DELETE)).contains(new Route(HttpMethod.DELETE, "/delete", handler));
        assertThat(result.getRoutes(HttpMethod.HEAD)).contains(new Route(HttpMethod.HEAD, "/head", handler));
        assertThat(result.getRoutes(HttpMethod.OPTIONS)).contains(new Route(HttpMethod.OPTIONS, "/options", handler));
        assertThat(result.getRoutes(HttpMethod.PATCH)).contains(new Route(HttpMethod.PATCH, "/patch", handler));
    }
}
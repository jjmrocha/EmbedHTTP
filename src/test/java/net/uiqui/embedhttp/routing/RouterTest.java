package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.server.Request;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class RouterTest {
    @Test
    void testConstructor() {
        // given
        HttpRequestHandler handler = (request, response) -> {
        };
        Route route1 = new Route(HttpMethod.GET, "/get", handler);
        Route route2 = new Route(HttpMethod.POST, "/post", handler);
        Route route3 = new Route(HttpMethod.PUT, "/put", handler);
        Route route4 = new Route(HttpMethod.DELETE, "/delete", handler);
        // when
        Router router = new Router(route1, route2, route3, route4);
        // then
        assertThat(router.getRoutes(HttpMethod.GET)).containsExactly(route1);
        assertThat(router.getRoutes(HttpMethod.POST)).containsExactly(route2);
        assertThat(router.getRoutes(HttpMethod.PUT)).containsExactly(route3);
        assertThat(router.getRoutes(HttpMethod.DELETE)).containsExactly(route4);
    }

    @Test
    void testGetRoutes() {
        // given
        HttpRequestHandler handler = (request, response) -> {
        };
        Route route1 = new Route(HttpMethod.GET, "/get", handler);
        Route route2 = new Route(HttpMethod.POST, "/post", handler);
        Router router = new Router(route1, route2);
        // when
        var getRoutes = router.getRoutes(HttpMethod.GET);
        var postRoutes = router.getRoutes(HttpMethod.POST);
        var putRoutes = router.getRoutes(HttpMethod.PUT);
        // then
        assertThat(getRoutes).containsExactly(route1);
        assertThat(postRoutes).containsExactly(route2);
        assertThat(putRoutes).isEmpty();
    }

    @Test
    void testFindRoute() {
        // given
        HttpRequestHandler handler = (request, response) -> {
        };
        Route route1 = new Route(HttpMethod.GET, "/get", handler);
        Route route2 = new Route(HttpMethod.PUT, "/put/:id", handler);
        Router router = new Router(route1, route2);
        Request request1 = new Request(HttpMethod.GET, "/get?name=value", emptyMap(), null);
        Request request2 = new Request(HttpMethod.PUT, "/put/123", emptyMap(), null);
        Request request3 = new Request(HttpMethod.POST, "/post", emptyMap(), null);
        Request request4 = new Request(HttpMethod.PUT, "/put", emptyMap(), null);
        // when
        var foundRoute1 = router.findRoute(request1);
        var foundRoute2 = router.findRoute(request2);
        var foundRoute3 = router.findRoute(request3);
        var foundRoute4 = router.findRoute(request4);
        // then
        assertThat(foundRoute1).isNotNull();
        assertThat(foundRoute1.getRoute()).isEqualTo(route1);
        assertThat(foundRoute1.getRequest()).isEqualTo(request1);
        assertThat(foundRoute2).isNotNull();
        assertThat(foundRoute2.getRoute()).isEqualTo(route2);
        assertThat(foundRoute2.getRequest()).isEqualTo(request2);
        assertThat(foundRoute2.getPathParameters()).containsEntry("id", "123");
        assertThat(foundRoute3).isNull();
        assertThat(foundRoute4).isNull();
    }
}
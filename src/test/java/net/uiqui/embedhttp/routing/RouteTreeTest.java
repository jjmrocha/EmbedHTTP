package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RouteTreeTest {
    @ParameterizedTest
    @MethodSource("pathTests")
    void testSplitPath(String path, List<String> expectedSegments) {
        // given
        var classUnderTest = new RouteTree();
        // when
        var result = classUnderTest.splitPath(path);
        // then
        assertThat(result).containsExactly(expectedSegments.toArray(new String[0]));
    }

    @ParameterizedTest
    @MethodSource("paramTests")
    void testExtractParameterName(String segment, String expectedParam) {
        // given
        var classUnderTest = new RouteTree();
        // when
        var result = classUnderTest.extractParameterName(segment);
        // then
        assertThat(result).isEqualTo(expectedParam);
    }

    @Test
    void testAddRootRoute() {
        // given
        var route = new Route(HttpMethod.GET, "/", null);
        var classUnderTest = new RouteTree();
        // when
        classUnderTest.addRoute(route);
        // then
        assertThat(classUnderTest.getAllRoutes()).containsExactly(route);
        assertThat(classUnderTest.getTreePaths()).containsExactly("/+");
    }

    @Test
    void testAddRoute() {
        // given
        var route = new Route(HttpMethod.GET, "/test/:param", null);
        var classUnderTest = new RouteTree();
        // when
        classUnderTest.addRoute(route);
        // then
        assertThat(classUnderTest.getAllRoutes()).containsExactly(route);
        assertThat(classUnderTest.getTreePaths()).containsExactly("/", "/test", "/test/:param+");
    }

    @Test
    void testAddRoutes() {
        // given
        var route0 = new Route(HttpMethod.GET, "/", null);
        var route1 = new Route(HttpMethod.POST, "/v1/events", null);
        var route2 = new Route(HttpMethod.GET, "/v1/events/:eventId", null);
        var route3 = new Route(HttpMethod.POST, "/v2/events", null);
        var route4 = new Route(HttpMethod.GET, "/v2/events/:eventId", null);
        var route5 = new Route(HttpMethod.POST, "/v2/events/:eventId/tickets", null);
        var route6 = new Route(HttpMethod.GET, "/v2/events/:eventId/tickets/:ticketId", null);
        var classUnderTest = new RouteTree();
        // when
        classUnderTest.addRoute(route0);
        classUnderTest.addRoute(route1);
        classUnderTest.addRoute(route2);
        classUnderTest.addRoute(route3);
        classUnderTest.addRoute(route4);
        classUnderTest.addRoute(route5);
        classUnderTest.addRoute(route6);
        // then
        assertThat(classUnderTest.getAllRoutes()).containsExactlyInAnyOrder(
                route0, route1, route2, route3, route4, route5, route6
        );
        assertThat(classUnderTest.getTreePaths()).containsExactlyInAnyOrder(
                "/+", "/v1", "/v1/events+", "/v1/events/:eventId+",
                "/v2", "/v2/events+", "/v2/events/:eventId+",
                "/v2/events/:eventId/tickets+", "/v2/events/:eventId/tickets/:ticketId+"
        );
    }

    @ParameterizedTest
    @MethodSource("findRouteTests")
    void testFindRoute(String path, String expectedPathPattern) {
        // given
        var route0 = new Route(HttpMethod.GET, "/", null);
        var route1 = new Route(HttpMethod.POST, "/v1/events", null);
        var route2 = new Route(HttpMethod.GET, "/v1/events/:eventId", null);
        var route3 = new Route(HttpMethod.POST, "/v2/events", null);
        var route4 = new Route(HttpMethod.GET, "/v2/events/:eventId", null);
        var route5 = new Route(HttpMethod.POST, "/v2/events/:eventId/tickets", null);
        var route6 = new Route(HttpMethod.GET, "/v2/events/:eventId/tickets/:ticketId", null);
        var classUnderTest = new RouteTree();
        // when
        classUnderTest.addRoute(route0);
        classUnderTest.addRoute(route1);
        classUnderTest.addRoute(route2);
        classUnderTest.addRoute(route3);
        classUnderTest.addRoute(route4);
        classUnderTest.addRoute(route5);
        classUnderTest.addRoute(route6);
        // when
        var result = classUnderTest.findRoute(path);
        // then
        if (result == null) {
            assertThat(expectedPathPattern).isNull();
        } else {
            assertThat(result.route().getPathPattern()).isEqualTo(expectedPathPattern);
        }
    }

    @Test
    void testWithoutRootRoute() {
        // given
        var route1 = new Route(HttpMethod.POST, "/v1/events", null);
        var classUnderTest = new RouteTree();
        classUnderTest.addRoute(route1);
        // when
        var result = classUnderTest.findRoute("/");
        // then
        assertThat(result).isNull();
    }

    private static Stream<Arguments> pathTests() {
        return Stream.of(
                // path, expectedSegments
                Arguments.of("/", List.of("/")),
                Arguments.of("segment", List.of("segment")),
                Arguments.of("/segment", List.of("segment")),
                Arguments.of("/segment/", List.of("segment")),
                Arguments.of(":param", List.of(":param")),
                Arguments.of("/:param", List.of(":param")),
                Arguments.of("/:param/", List.of(":param")),
                Arguments.of("/v1/segment", List.of("v1", "segment")),
                Arguments.of("/v1/:param", List.of("v1", ":param")),
                Arguments.of("/v1/segment/:param/", List.of("v1", "segment", ":param"))
        );
    }

    private static Stream<Arguments> paramTests() {
        return Stream.of(
                // segment, expectedParam
                Arguments.of(":param", "param"),
                Arguments.of(":a1", "a1"),
                Arguments.of(":aZ", "aZ"),
                Arguments.of(":Z2", "Z2"),
                Arguments.of(":Za", "Za"),
                Arguments.of("staticSegment", null),
                Arguments.of("anotherStaticSegment", null)
        );
    }

    private static Stream<Arguments> findRouteTests() {
        return Stream.of(
                // path, expectedPathPattern
                Arguments.of("/", "/"),
                Arguments.of("/v1/events", "/v1/events"),
                Arguments.of("/v1/events/", "/v1/events"),
                Arguments.of("/v1/events/123", "/v1/events/:eventId"),
                Arguments.of("/v1/events/123/", "/v1/events/:eventId"),
                Arguments.of("/v2/events", "/v2/events"),
                Arguments.of("/v2/events/456", "/v2/events/:eventId"),
                Arguments.of("/v2/events/456/tickets", "/v2/events/:eventId/tickets"),
                Arguments.of("/v2/events/456/tickets/789", "/v2/events/:eventId/tickets/:ticketId"),
                Arguments.of("/v1/unknown", null),
                Arguments.of("/v3", null),
                Arguments.of("/v2/events/456/tickets/789/unknown", null)
        );
    }
}
package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

class PathSegmentTest {
    @Test
    void testRootSegment() {
        // when
        var classUnderTest = new PathSegment.Root();
        // then
        assertThat(classUnderTest.parent).isNull();
        assertThat(classUnderTest.hasRoute()).isFalse();
        assertThat(classUnderTest.getRoute()).isNull();
        assertThat(classUnderTest.toString()).hasToString("/");
        assertThat(classUnderTest.getChildCount()).isZero();
        assertThat(classUnderTest.getAllRoutes()).isEmpty();
        assertThat(classUnderTest.getTreePaths()).containsExactly("/");
    }

    @Test
    void testStaticSegmentWithRoute() {
        // given
        var classUnderTest = new PathSegment.Root();
        var route = new Route(HttpMethod.GET, "/test", null);
        // when
        var result = classUnderTest.registerStaticChild("test", route);
        // then
        assertThat(classUnderTest.getChildCount()).isEqualTo(1);
        assertThat(classUnderTest.findChild("test", true)).isSameAs(result);
        assertThat(classUnderTest.getAllRoutes()).containsExactly(route);
        assertThat(classUnderTest.getTreePaths()).containsExactly("/", "/test+");
        assertThat(result.parent).isEqualTo(classUnderTest);
        assertThat(result.hasRoute()).isTrue();
        assertThat(result.getRoute()).isEqualTo(route);
        assertThat(result.toString()).hasToString("/test");
        assertThat(result.getChildCount()).isZero();
        assertThat(result.getAllRoutes()).containsExactly(route);
        assertThat(result.getTreePaths()).containsExactly("/test+");
    }

    @Test
    void testStaticSegmentWithoutRoute() {
        // given
        var classUnderTest = new PathSegment.Root();
        // when
        var result = classUnderTest.registerStaticChild("test", null);
        // then
        assertThat(classUnderTest.getChildCount()).isEqualTo(1);
        assertThat(classUnderTest.findChild("test", true)).isSameAs(result);
        assertThat(classUnderTest.getAllRoutes()).isEmpty();
        assertThat(classUnderTest.getTreePaths()).containsExactly("/", "/test");
        assertThat(result.parent).isEqualTo(classUnderTest);
        assertThat(result.hasRoute()).isFalse();
        assertThat(result.getRoute()).isNull();
        assertThat(result.toString()).hasToString("/test");
        assertThat(result.getChildCount()).isZero();
        assertThat(result.getAllRoutes()).isEmpty();
        assertThat(result.getTreePaths()).containsExactly("/test");
    }

    @Test
    void testParameterSegmentWithRoute() {
        // given
        var classUnderTest = new PathSegment.Root();
        var route = new Route(HttpMethod.GET, "/:name", null);
        // when
        var result = classUnderTest.registerParameterChild("name", route);
        // then
        assertThat(classUnderTest.getChildCount()).isEqualTo(1);
        assertThat(classUnderTest.findChild("test", true)).isNull();
        assertThat(classUnderTest.findParameterChild()).isSameAs(result);
        assertThat(classUnderTest.findChild("test", false)).isSameAs(result);
        assertThat(classUnderTest.getAllRoutes()).containsExactly(route);
        assertThat(classUnderTest.getTreePaths()).containsExactly("/", "/:name+");
        assertThat(result.parent).isEqualTo(classUnderTest);
        assertThat(result.hasRoute()).isTrue();
        assertThat(result.getRoute()).isEqualTo(route);
        assertThat(result.toString()).hasToString("/:name");
        assertThat(result.getChildCount()).isZero();
        assertThat(result.getAllRoutes()).containsExactly(route);
        assertThat(result.getTreePaths()).containsExactly("/:name+");
    }

    @Test
    void testParameterSegmentWithoutRoute() {
        // given
        var classUnderTest = new PathSegment.Root();
        // when
        var result = classUnderTest.registerParameterChild("name", null);
        // then
        assertThat(classUnderTest.getChildCount()).isEqualTo(1);
        assertThat(classUnderTest.findChild("test", true)).isNull();
        assertThat(classUnderTest.findParameterChild()).isSameAs(result);
        assertThat(classUnderTest.findChild("test", false)).isSameAs(result);
        assertThat(classUnderTest.getAllRoutes()).isEmpty();
        assertThat(classUnderTest.getTreePaths()).containsExactly("/", "/:name");
        assertThat(result.parent).isEqualTo(classUnderTest);
        assertThat(result.hasRoute()).isFalse();
        assertThat(result.getRoute()).isNull();
        assertThat(result.toString()).hasToString("/:name");
        assertThat(result.getChildCount()).isZero();
        assertThat(result.getAllRoutes()).isEmpty();
        assertThat(result.getTreePaths()).containsExactly("/:name");
    }

    @Test
    void testThreeStaticSegments() {
        // given
        var route = new Route(HttpMethod.GET, "/test1/test2/test3", null);
        var classUnderTest = new PathSegment.Root();
        // when
        var segment1 = classUnderTest.registerStaticChild("test1", null);
        var segment2 = segment1.registerStaticChild("test2", null);
        var segment3 = segment2.registerStaticChild("test3", route);
        // then
        assertThat(classUnderTest.getAllRoutes()).containsExactly(route);
        assertThat(classUnderTest.getTreePaths()).containsExactly("/", "/test1", "/test1/test2", "/test1/test2/test3+");
        assertThat(segment3.toString()).hasToString("/test1/test2/test3");
    }

    @Test
    void testThreeParameterSegments() {
        // given
        var route = new Route(HttpMethod.GET, "/:name1/:name2/:name3", null);
        var classUnderTest = new PathSegment.Root();
        // when
        var segment1 = classUnderTest.registerParameterChild("name1", null);
        var segment2 = segment1.registerParameterChild("name2", null);
        var segment3 = segment2.registerParameterChild("name3", route);
        // then
        assertThat(classUnderTest.getAllRoutes()).containsExactly(route);
        assertThat(classUnderTest.getTreePaths()).containsExactly("/", "/:name1", "/:name1/:name2", "/:name1/:name2/:name3+");
        assertThat(segment3.toString()).hasToString("/:name1/:name2/:name3");
    }

    @Test
    void testAddDuplicatedSegments() {
        // given
        var classUnderTest = new PathSegment.Root();
        // when
        classUnderTest.registerStaticChild("test", null);
        // then
        var response = catchThrowable(() ->
                classUnderTest.registerStaticChild("test", null)
        );
        // when
        assertThat(response).isInstanceOf(InvalidRouteException.class)
                .hasMessage("Path segment 'test' already exists at '/'.");
    }

    @Test
    void testAddDuplicatedMatchAll() {
        // given
        var classUnderTest = new PathSegment.Root();
        // when
        classUnderTest.registerParameterChild("clientId", null);
        // then
        var response = catchThrowable(() ->
                classUnderTest.registerParameterChild("eventId", null)
        );
        // when
        assertThat(response).isInstanceOf(InvalidRouteException.class)
                .hasMessage("Path segment '*' already exists at '/'.");
    }

    @Test
    void testSetRouteOnRoot() {
        // given
        var classUnderTest = new PathSegment.Root();
        var route = new Route(HttpMethod.GET, "/", null);
        // when
        classUnderTest.setRoute(route);
        // then
        assertThat(classUnderTest.hasRoute()).isTrue();
        assertThat(classUnderTest.getRoute()).isEqualTo(route);
        assertThat(classUnderTest.getAllRoutes()).containsExactly(route);
    }

    @Test
    void testSetRouteOnRootWhenRouteAlreadyExists() {
        // given
        var classUnderTest = new PathSegment.Root();
        var route1 = new Route(HttpMethod.GET, "/", null);
        classUnderTest.setRoute(route1);
        // when
        var route2 = new Route(HttpMethod.POST, "/", null);
        var response = catchThrowable(() -> classUnderTest.setRoute(route2));
        // then
        assertThat(response).isInstanceOf(InvalidRouteException.class)
                .hasMessage("Path segment '/' already has a handler.");
    }
}
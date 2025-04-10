package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.api.HttpMethod;

import java.util.ArrayList;
import java.util.List;

public class RoutingBuilder {
    private final List<Route> routeList = new ArrayList<>();

    private RoutingBuilder() {
        // Private constructor to prevent instantiation
    }

    public static RoutingBuilder newRouter() {
        return new RoutingBuilder();
    }

    private  RoutingBuilder withRoute(HttpMethod method, String pathPattern, HttpRequestHandler handler) {
        var route = new Route(
                method,
                pathPattern,
                handler
        );
        routeList.add(route);
        return this;
    }

    public RoutingBuilder get(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.GET, pathPattern, handler);
    }

    public RoutingBuilder post(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.POST, pathPattern, handler);
    }

    public RoutingBuilder put(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.PUT, pathPattern, handler);
    }

    public RoutingBuilder delete(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.DELETE, pathPattern, handler);
    }

    public RoutingBuilder head(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.HEAD, pathPattern, handler);
    }

    public RoutingBuilder options(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.OPTIONS, pathPattern, handler);
    }

    public RoutingBuilder patch(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.PATCH, pathPattern, handler);
    }

    public Router build() {
        return new Router(routeList);
    }
}

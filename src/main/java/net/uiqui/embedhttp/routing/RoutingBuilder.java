package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;

import java.util.EnumMap;
import java.util.Map;

public abstract class RoutingBuilder implements Router {
    private static final RouteTree EMPTY_ROUTE_TREE = new RouteTree();
    private final Map<HttpMethod, RouteTree> routingTable = new EnumMap<>(HttpMethod.class);

    @Override
    public RoutingBuilder withRoute(HttpMethod method, String pathPattern, HttpRequestHandler handler) {
        var route = new Route(method, pathPattern, handler);
        routingTable.computeIfAbsent(method, k -> new RouteTree())
                .addRoute(route);
        return this;
    }

    @Override
    public RoutingBuilder get(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.GET, pathPattern, handler);
    }

    @Override
    public RoutingBuilder post(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.POST, pathPattern, handler);
    }

    @Override
    public RoutingBuilder put(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.PUT, pathPattern, handler);
    }

    @Override
    public RoutingBuilder delete(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.DELETE, pathPattern, handler);
    }

    @Override
    public RoutingBuilder head(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.HEAD, pathPattern, handler);
    }

    @Override
    public RoutingBuilder options(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.OPTIONS, pathPattern, handler);
    }

    @Override
    public RoutingBuilder patch(String pathPattern, HttpRequestHandler handler) {
        return withRoute(HttpMethod.PATCH, pathPattern, handler);
    }

    public RouteTree getRouteTreeForMethod(HttpMethod method) {
        return routingTable.getOrDefault(method, EMPTY_ROUTE_TREE);
    }
}

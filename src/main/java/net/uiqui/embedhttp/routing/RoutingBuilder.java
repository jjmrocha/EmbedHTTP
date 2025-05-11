package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public abstract class RoutingBuilder implements Router {
    private final Map<HttpMethod, List<Route>> routingTable = new EnumMap<>(HttpMethod.class);

    private RoutingBuilder withRoute(HttpMethod method, String pathPattern, HttpRequestHandler handler) {
        var route = new Route(method, pathPattern, handler);
        routingTable.computeIfAbsent(method, k -> new ArrayList<>())
                .add(route);
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

    public List<Route> getRoutesForMethod(HttpMethod method) {
        return routingTable.getOrDefault(method, emptyList());
    }
}

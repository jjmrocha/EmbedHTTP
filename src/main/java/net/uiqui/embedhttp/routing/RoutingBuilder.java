package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.HttpRequestHandler;

import java.util.ArrayList;
import java.util.List;

public class RoutingBuilder {
    private final List<Route> routingTable = new ArrayList<>();

    public static RoutingBuilder newRouting() {
        return new RoutingBuilder();
    }

    public RoutingBuilder withRoute(HttpMethod method, String path, HttpRequestHandler handler) {
        var route = new Route(
                method,
                path,
                handler
        );
        routingTable.add(route);
        return this;
    }

    public List<Route> build() {
        return routingTable;
    }
}

package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.impl.HttpRequestImpl;
import net.uiqui.embedhttp.server.Request;

public class RouterImpl extends RoutingBuilder {
    public RouterImpl() {
        super();
    }

    public HttpRequestImpl routeRequest(Request request) {
        var routeTree = getRouteTreeForMethod(request.getMethod());
        var routeMatch = routeTree.findRoute(request.getPath());

        if (routeMatch == null) {
            return null;
        }

        return new HttpRequestImpl(request, routeMatch.route(), routeMatch.pathParameters());
    }
}

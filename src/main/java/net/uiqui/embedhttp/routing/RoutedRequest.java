package net.uiqui.embedhttp.routing;

import java.util.Map;

public class RoutedRequest {
    private final Request request;
    private final Route route;
    private final Map<String, String> pathParameters;

    public RoutedRequest(Request request, Route route, Map<String, String> pathParameters) {
        this.request = request;
        this.route = route;
        this.pathParameters = pathParameters;
    }

    public Request getRequest() {
        return request;
    }

    public Route getRoute() {
        return route;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }
}

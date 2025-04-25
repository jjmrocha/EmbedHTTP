package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.impl.HttpRequestImpl;
import net.uiqui.embedhttp.server.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class Router {
    private final Map<HttpMethod, List<Route>> routingTable;

    public Router(List<Route> routeList) {
        this.routingTable = routeList.stream().collect(Collectors.groupingBy(Route::getMethod));
    }

    public Router(Route... routes) {
        this(List.of(routes));
    }

    public List<Route> getRoutes(HttpMethod method) {
        return routingTable.getOrDefault(method, emptyList());
    }

    public HttpRequestImpl findRoute(Request request) {
        var validRoutes = getRoutes(request.getMethod());

        if (validRoutes.isEmpty()) {
            return null;
        }

        for (Route route : validRoutes) {
            var matcher = route.getPathRegexPattern().matcher(request.getPath());

            if (!matcher.matches()) {
                continue;
            }

            var pathParameters = new HashMap<String, String>(matcher.groupCount());

            for (Map.Entry<String, Integer> matchedGroup : matcher.namedGroups().entrySet()) {
                var key = matchedGroup.getKey();
                var value = matcher.group(matchedGroup.getValue());
                pathParameters.put(key, value);
            }

            return new HttpRequestImpl(request, route, pathParameters);
        }

        return null;
    }
}

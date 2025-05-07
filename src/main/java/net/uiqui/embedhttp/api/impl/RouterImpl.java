package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.server.InsensitiveMap;
import net.uiqui.embedhttp.server.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class RouterImpl extends RoutingBuilder {
    public RouterImpl() {
        super();
    }

    public HttpRequestImpl routeRequest(Request request) {
        var validRoutes = getRoutesForMethod(request.getMethod());

        if (validRoutes.isEmpty()) {
            return null;
        }

        for (var route : validRoutes) {
            var matcher = route.getPathRegexPattern().matcher(request.getPath());

            if (!matcher.matches()) {
                continue;
            }

            var pathParameters = extractPathParameters(matcher);

            return new HttpRequestImpl(request, route, pathParameters);
        }

        return null;
    }

    private static Map<String, String> extractPathParameters(Matcher matcher) {
        HashMap<String, String> pathParameters = HashMap.newHashMap(matcher.groupCount());

        for (var matchedGroup : matcher.namedGroups().entrySet()) {
            var key = matchedGroup.getKey();
            var value = matcher.group(matchedGroup.getValue());
            pathParameters.put(key, value);
        }

        return pathParameters;
    }
}

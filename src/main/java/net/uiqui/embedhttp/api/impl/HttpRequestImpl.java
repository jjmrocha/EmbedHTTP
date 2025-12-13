package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequest;
import net.uiqui.embedhttp.routing.Route;
import net.uiqui.embedhttp.server.Request;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestImpl implements HttpRequest {
    private final Request request;
    private final Route route;
    private final Map<String, String> pathParameters;
    private final Lazy<Map<String, String>> queryParameters = Lazy.of(this::extractQueryParameters);

    public HttpRequestImpl(Request request, Route route, Map<String, String> pathParameters) {
        this.request = request;
        this.route = route;
        this.pathParameters = pathParameters;
    }

    @Override
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    @Override
    public String getURL() {
        return request.getUrl();
    }

    @Override
    public String getPath() {
        return request.getPath();
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return queryParameters.get();
    }

    @Override
    public Map<String, String> getHeaders() {
        return request.getHeaders();
    }

    @Override
    public String getBody() {
        return request.getBody();
    }

    @Override
    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public Request getRequest() {
        return request;
    }

    public Route getRoute() {
        return route;
    }

    protected Map<String, String> extractQueryParameters() {
        var parameters = new HashMap<String, String>();
        var query = request.getQuery();

        if (query.isEmpty()) {
            return parameters;
        }

        var queryParts = query.split("&");
        for (var part : queryParts) {
            if (part.isEmpty()) continue;

            var splitIndex = part.indexOf('=');
            if (splitIndex == -1) {
                // Handle valueless parameters: ?flag
                var key = decodeParam(part);
                parameters.put(key, "");
            } else {
                var key = decodeParam(part.substring(0, splitIndex));
                var value = decodeParam(part.substring(splitIndex + 1));
                parameters.put(key, value);
            }
        }

        return parameters;
    }

    private static String decodeParam(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

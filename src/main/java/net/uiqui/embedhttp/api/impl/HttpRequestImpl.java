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
        var queryParts = request.getQuery().split("&");

        for (var part : queryParts) {
            var keyValue = part.split("=");

            if (keyValue.length == 2) {
                var key = keyValue[0];
                var value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                parameters.put(key, value);
            }
        }

        return parameters;
    }
}

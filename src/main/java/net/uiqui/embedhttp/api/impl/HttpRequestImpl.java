package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequest;
import net.uiqui.embedhttp.server.Request;
import net.uiqui.embedhttp.routing.Route;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public record HttpRequestImpl(Request request, Route route, Map<String, String> pathParameters) implements HttpRequest {
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
        var queryParameters = new HashMap<String, String>();
        var queryParts = request.getQuery().split("&");

        for (String part : queryParts) {
            var keyValue = part.split("=");

            if (keyValue.length == 2) {
                var key = keyValue[0];
                var value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                queryParameters.put(key, value);
            }
        }

        return queryParameters;
    }

    @Override
    public Map<String, String> getHeaders() {
        return request.getHeaders();
    }

    @Override
    public String getBody() {
        return request.getBody();
    }
}

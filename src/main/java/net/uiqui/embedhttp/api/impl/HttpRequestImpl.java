package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequest;

import java.util.Map;

public class HttpRequestImpl implements HttpRequest {
    @Override
    public HttpMethod getMethod() {
        return null;
    }

    @Override
    public String getURL() {
        return "";
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public Map<String, String> getPathParameters() {
        return Map.of();
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return Map.of();
    }

    @Override
    public Map<String, String> getHeaders() {
        return Map.of();
    }

    @Override
    public String getBody() {
        return "";
    }
}

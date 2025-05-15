package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpMethod;

import java.util.Map;

public class Request {
    private final HttpMethod method;
    private final String url;
    private final InsensitiveMap headers;
    private final String body;
    private final String path;
    private final String query;
    private final boolean keepAlive;

    public Request(HttpMethod method, String url, InsensitiveMap headers, String body, boolean keepAlive) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;

        var queryIndex = url.indexOf('?');

        if (queryIndex != -1) {
            this.path = url.substring(0, queryIndex);
            this.query = url.substring(queryIndex + 1);
        } else {
            this.path = url;
            this.query = "";
        }

        this.keepAlive = keepAlive;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }
}

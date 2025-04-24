package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;

import java.util.Map;

public class Request {
    private final HttpMethod method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;

    public Request(HttpMethod method, String url, Map<String, String> headers, String body) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
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
}

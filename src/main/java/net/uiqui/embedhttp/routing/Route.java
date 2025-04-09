package net.uiqui.embedhttp.routing;

import java.util.regex.Pattern;
import net.uiqui.embedhttp.HttpRequestHandler;

public class Route {
    private final HttpMethod method;
    private final String path;
    private final Pattern pathPattern;
    private final HttpRequestHandler handler;

    public Route(HttpMethod method, String path, HttpRequestHandler handler) {
        this.method = method;
        this.path = path;
        this.pathPattern = Pattern.compile(path);
        this.handler = handler;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Pattern getPathPattern() {
        return pathPattern;
    }

    public HttpRequestHandler getHandler() {
        return handler;
    }
}

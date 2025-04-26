package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;

import java.util.Objects;
import java.util.regex.Pattern;

public class Route {
    private final HttpMethod method;
    private final String pathPattern;
    private final Pattern pathRegexPattern;
    private final HttpRequestHandler handler;

    public Route(HttpMethod method, String pathPattern, HttpRequestHandler handler) {
        this.method = method;
        this.pathPattern = pathPattern;
        this.pathRegexPattern = PathPatternCompiler.compile(pathPattern);
        this.handler = handler;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public Pattern getPathRegexPattern() {
        return pathRegexPattern;
    }

    public HttpRequestHandler getHandler() {
        return handler;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return method == route.method && Objects.equals(pathPattern, route.pathPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, pathPattern);
    }
}

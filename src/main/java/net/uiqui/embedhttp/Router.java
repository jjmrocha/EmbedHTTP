package net.uiqui.embedhttp;

import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.api.impl.RouterImpl;

public interface Router {
    Router get(String pathPattern, HttpRequestHandler handler);

    Router post(String pathPattern, HttpRequestHandler handler);

    Router put(String pathPattern, HttpRequestHandler handler);

    Router delete(String pathPattern, HttpRequestHandler handler);

    Router head(String pathPattern, HttpRequestHandler handler);

    Router options(String pathPattern, HttpRequestHandler handler);

    Router patch(String pathPattern, HttpRequestHandler handler);

    static Router newRouter() {
        return new RouterImpl();
    }
}

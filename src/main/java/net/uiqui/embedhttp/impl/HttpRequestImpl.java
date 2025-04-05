package net.uiqui.embedhttp.impl;

import net.uiqui.embedhttp.HttpRequest;

import java.util.Map;

public class HttpRequestImpl implements HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final String body;

    private HttpRequestImpl(Builder builder) {
        method = builder.method;
        path = builder.path;
        queryParams = builder.queryParams;
        headers = builder.headers;
        body = builder.body;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return queryParams;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getBody() {
        return body;
    }

    public static final class Builder {
        private String method;
        private String path;
        private Map<String, String> queryParams;
        private Map<String, String> headers;
        private String body;

        public Builder() {
        }

        public Builder withMethod(String val) {
            method = val;
            return this;
        }

        public Builder withPath(String val) {
            path = val;
            return this;
        }

        public Builder withQueryParams(Map<String, String> val) {
            queryParams = val;
            return this;
        }

        public Builder withHeaders(Map<String, String> val) {
            headers = val;
            return this;
        }

        public Builder withBody(String val) {
            body = val;
            return this;
        }

        public HttpRequestImpl build() {
            return new HttpRequestImpl(this);
        }
    }
}

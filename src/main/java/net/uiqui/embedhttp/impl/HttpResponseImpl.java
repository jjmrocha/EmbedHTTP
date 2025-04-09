package net.uiqui.embedhttp.impl;

import net.uiqui.embedhttp.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpResponseImpl implements HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private Map<String, String> headers = new HashMap<>();
    private String body;

    @Override
    public void setStatus(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    @Override
    public void setHeader(String name, String value) {
        Objects.requireNonNull(name, "Header name cannot be null");
        Objects.requireNonNull(value, "Header value cannot be null");
        headers.put(name, value);
    }

    @Override
    public void setContentType(String contentType) {
        Objects.requireNonNull(contentType, "Content-Type cannot be null");
        setHeader("Content-Type", contentType);
    }

    @Override
    public void setBody(String body) {
        Objects.requireNonNull(body, "Body cannot be null");
        setHeader("Content-Length", String.valueOf(body.length()));
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}

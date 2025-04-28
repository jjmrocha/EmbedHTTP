package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.HttpStatusCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpResponseImpl implements HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private String body;

    @Override
    public void setStatus(HttpStatusCode statusCode) {
        Objects.requireNonNull(statusCode, "Status code cannot be null");
        setStatus(statusCode.getCode(), statusCode.getReasonPhrase());
    }

    @Override
    public void setStatus(int statusCode, String statusMessage) {
        Objects.requireNonNull(statusMessage, "Status message cannot be null");
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    @Override
    public void setHeader(HttpHeader name, String value) {
        Objects.requireNonNull(name, "Header name cannot be null");
        Objects.requireNonNull(value, "Header value cannot be null");
        setHeader(name.getValue(), value);
    }

    @Override
    public void setHeader(String name, String value) {
        Objects.requireNonNull(name, "Header name cannot be null");
        Objects.requireNonNull(value, "Header value cannot be null");
        headers.put(name, value);
    }

    @Override
    public void setContentType(ContentType contentType) {
        Objects.requireNonNull(contentType, "Content-Type cannot be null");
        setContentType(contentType.getValue());
    }

    @Override
    public void setContentType(String contentType) {
        Objects.requireNonNull(contentType, "Content-Type cannot be null");
        setHeader(HttpHeader.CONTENT_TYPE, contentType);
    }

    @Override
    public void setBody(ContentType contentType, String body) {
        Objects.requireNonNull(contentType, "Content-Type cannot be null");
        Objects.requireNonNull(body, "Body cannot be null");
        setContentType(contentType);
        setBody(body);
    }

    @Override
    public void setBody(String body) {
        Objects.requireNonNull(body, "Body cannot be null");
        setHeader(HttpHeader.CONTENT_LENGTH, String.valueOf(body.length()));
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

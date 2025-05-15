package net.uiqui.embedhttp.api.impl;

import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.HttpStatusCode;
import net.uiqui.embedhttp.server.InsensitiveMap;
import net.uiqui.embedhttp.server.io.ConnectionHeader;

import java.util.Map;
import java.util.Objects;

public class HttpResponseImpl implements HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final Map<String, String> headers = new InsensitiveMap();
    private String body = null;
    private boolean closeConnection = false;

    public HttpResponseImpl(HttpStatusCode statusCode) {
        this(statusCode.getCode(), statusCode.getReasonPhrase());
    }

    public HttpResponseImpl(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    @Override
    public HttpResponse setHeader(HttpHeader name, String value) {
        if (HttpHeader.CONNECTION == name) {
            closeConnection = ConnectionHeader.CLOSE.getValue().equals(value);
        }

        return setHeader(name.getValue(), value);
    }

    @Override
    public HttpResponse setHeader(String name, String value) {
        Objects.requireNonNull(name, "Header name cannot be null");
        Objects.requireNonNull(value, "Header value cannot be null");
        headers.put(name, value);
        return this;
    }

    @Override
    public HttpResponse setBody(ContentType contentType, String body) {
        return setBody(contentType.getValue(), body);
    }

    @Override
    public HttpResponse setBody(String contentType, String body) {
        Objects.requireNonNull(contentType, "Content-Type cannot be null");
        Objects.requireNonNull(body, "Body cannot be null");
        setHeader(HttpHeader.CONTENT_TYPE, contentType);
        setHeader(HttpHeader.CONTENT_LENGTH, String.valueOf(body.length()));
        this.body = body;
        return this;
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

    public boolean closeConnection() {
        return closeConnection;
    }
}

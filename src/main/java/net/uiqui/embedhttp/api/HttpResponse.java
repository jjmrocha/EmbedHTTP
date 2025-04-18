package net.uiqui.embedhttp.api;

public interface HttpResponse {
    void setStatus(int statusCode, String statusMessage);
    void setHeader(String name, String value);
    void setContentType(String contentType);
    void setBody(String body);
}

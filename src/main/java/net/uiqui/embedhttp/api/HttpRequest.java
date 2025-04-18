package net.uiqui.embedhttp.api;

import java.util.Map;

public interface HttpRequest {
    HttpMethod getMethod();
    String getURL();
    String getPath();
    Map<String, String> getPathParameters();
    Map<String, String> getQueryParameters();
    Map<String, String> getHeaders();
    String getBody();
}

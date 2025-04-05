package net.uiqui.embedhttp;

import java.util.Map;

public interface HttpRequest {
    String getMethod();
    String getPath();
    Map<String, String> getQueryParameters();
    Map<String, String> getHeaders();
    String getBody();
}

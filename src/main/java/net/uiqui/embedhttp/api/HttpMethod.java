package net.uiqui.embedhttp.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the HTTP methods supported by the server.
 */
public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    OPTIONS,
    HEAD;

    private static final Map<String, HttpMethod> METHOD_MAP = HashMap.newHashMap(HttpMethod.values().length);

    static {
        for (HttpMethod method : HttpMethod.values()) {
            METHOD_MAP.put(method.name(), method);
        }
    }

    /**
     * Converts a string to its corresponding HttpMethod enum value.
     *
     * @param method the HTTP method as a string
     * @return the corresponding HttpMethod enum value, or null if the method is not recognized
     */
    public static HttpMethod fromString(String method) {
        if (method == null || method.isEmpty()) {
            return null;
        }

        var upperCaseMethod = method.toUpperCase();
        return METHOD_MAP.get(upperCaseMethod);
    }
}

package net.uiqui.embedhttp.routing;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    OPTIONS,
    HEAD;

    public static HttpMethod fromString(String method) {
        var upperCaseMethod = method.toUpperCase();

        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (httpMethod.name().equals(upperCaseMethod)) {
                return httpMethod;
            }
        }

        return null;
    }
}

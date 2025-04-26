package net.uiqui.embedhttp.api;

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

    /**
     * Converts a string to its corresponding HttpMethod enum value.
     *
     * @param method the HTTP method as a string
     * @return the corresponding HttpMethod enum value
     * @throws IllegalArgumentException if the method is not recognized
     */
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

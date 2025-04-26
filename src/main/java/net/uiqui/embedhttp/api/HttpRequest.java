package net.uiqui.embedhttp.api;

import java.util.Map;

/**
 * Class representing an HTTP request.
 * Contains details such as the HTTP method, URL, headers, and body.
 */
public interface HttpRequest {
    /**
     * Gets the HTTP method of the request.
     *
     * @return the HTTP method
     */
    HttpMethod getMethod();

    /**
     * Gets the full URL of the request.
     *
     * @return the URL as a string
     */
    String getURL();

    /**
     * Gets the path of the request (excluding query parameters).
     *
     * @return the path as a string
     */
    String getPath();

    /**
     * Gets the path parameters extracted from the URL.
     *
     * @return a map of path parameter names to their values
     */
    Map<String, String> pathParameters();

    /**
     * Gets the query parameters from the URL.
     *
     * @return a map of query parameter names to their values
     */
    Map<String, String> getQueryParameters();

    /**
     * Gets the headers of the request.
     *
     * @return a map of header names to their values
     */
    Map<String, String> getHeaders();

    /**
     * Gets the body of the request.
     *
     * @return the body as a string
     */
    String getBody();
}

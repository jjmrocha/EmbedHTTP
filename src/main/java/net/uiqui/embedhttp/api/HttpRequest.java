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
    Map<String, String> getPathParameters();

    /**
     * Gets a specific path parameter by name.
     *
     * @param name the name of the path parameter
     * @return the value of the path parameter, or null if not found
     */
    default String getPathParameter(String name) {
        return getPathParameters().get(name);
    }

    /**
     * Gets the query parameters from the URL.
     *
     * @return a map of query parameter names to their values
     */
    Map<String, String> getQueryParameters();

    /**
     * Gets a specific query parameter by name.
     *
     * @param name the name of the query parameter
     * @return the value of the query parameter, or null if not found
     */
    default String getQueryParameter(String name) {
        return getQueryParameters().get(name);
    }

    /**
     * Gets the headers of the request.
     *
     * @return a map of header names to their values
     */
    Map<String, String> getHeaders();

    /**
     * Gets a specific header by name.
     *
     * @param name the name of the header
     * @return the value of the header, or null if not found
     */
    default String getHeader(String name) {
        return getHeaders().get(name);
    }

    /**
     * Gets the content type of the request.
     *
     * @return the content type as a string
     */
    default String getContentType() {
        return getHeader(HttpHeader.CONTENT_TYPE.getValue());
    }

    /**
     * Gets the accept header of the request.
     *
     * @return the accept header as a string
     */
    default String getAccept() {
        return getHeader(HttpHeader.ACCEPT.getValue());
    }

    /**
     * Gets the body of the request.
     *
     * @return the body as a string
     */
    String getBody();
}

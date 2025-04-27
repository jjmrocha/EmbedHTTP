package net.uiqui.embedhttp.api;

import net.uiqui.embedhttp.ContentType;

/**
 * Class representing an HTTP response.
 * Contains details such as the status code, headers, and body.
 */
public interface HttpResponse {
    /**
     * Gets the body of the request.
     *
     * @return the body as a string
     */
    void setStatus(HttpStatusCode statusCode);

    /**
     * Sets the HTTP status code and custom status message for the response.
     *
     * @param statusCode    the HTTP status code
     * @param statusMessage the custom status message
     */
    void setStatus(int statusCode, String statusMessage);

    /**
     * Sets a header for the response.
     *
     * @param name  the header name as an enum
     * @param value the header value
     */
    void setHeader(HttpHeader name, String value);

    /**
     * Sets a header for the response.
     *
     * @param name  the header name as a string
     * @param value the header value
     */
    void setHeader(String name, String value);

    /**
     * Sets the content type of the response.
     *
     * @param contentType the content type
     */
    void setContentType(ContentType contentType);

    /**
     * Sets the content type of the response.
     *
     * @param contentType the content type as a string
     */
    void setContentType(String contentType);

    /**
     * Sets the body of the response.
     *
     * @param body the body as a string
     */
    void setBody(String body);

    /**
     * Sets the body of the response with a specific content type.
     *
     * @param contentType the content type
     * @param body        the body as a string
     */
    void setBody(ContentType contentType, String body);
}

package net.uiqui.embedhttp.api;

import net.uiqui.embedhttp.api.impl.HttpResponseImpl;

/**
 * Class representing an HTTP response.
 * Contains details such as the status code, headers, and body.
 */
public interface HttpResponse {
    /**
     * Sets a header for the response.
     *
     * @param name  the header name as an enum
     * @param value the header value
     */
    HttpResponse setHeader(HttpHeader name, String value);

    /**
     * Sets a header for the response.
     *
     * @param name  the header name as a string
     * @param value the header value
     */
    HttpResponse setHeader(String name, String value);

    /**
     * Sets the body of the response with a specific content type.
     *
     * @param contentType the content type
     * @param body        the body as a string
     */
    HttpResponse setBody(ContentType contentType, String body);

    /**
     * Sets the body of the response with a specific content type.
     *
     * @param contentType the content type as a string
     * @param body        the body as a string
     */
    HttpResponse setBody(String contentType, String body);

    /**
     * Creates a new HTTP response with the specified status code and default message.
     *
     * @param statusCode the HTTP status code
     * @return a new HttpResponse instance
     */
    static HttpResponse withStatus(HttpStatusCode statusCode) {
        return new HttpResponseImpl(statusCode);
    }

    /**
     * Creates a new HTTP response with the specified status code and message.
     *
     * @param statusCode    the HTTP status code
     * @param statusMessage the HTTP status message
     * @return a new HttpResponse instance
     */
    static HttpResponse withStatus(int statusCode, String statusMessage) {
        return new HttpResponseImpl(statusCode, statusMessage);
    }

    /**
     * Creates a new HTTP response with the status code 200 (OK) and the specified body.
     *
     * @param contentType the content type
     * @param body        the body as a string
     * @return a new HttpResponse instance
     */
    static HttpResponse ok(ContentType contentType, String body) {
        return withStatus(HttpStatusCode.OK)
                .setBody(contentType, body);
    }

    /**
     * Creates a new HTTP response with the status code 500 (Internal Server Error) and the specified body.
     *
     * @return a new HttpResponse instance
     */
    static HttpResponse noContent() {
        return withStatus(HttpStatusCode.NO_CONTENT);
    }

    /**
     * Creates a new HTTP response with the status code 404 (Not Found) and the specified body.
     *
     * @param contentType the content type
     * @param body        the body as a string
     * @return a new HttpResponse instance
     */
    static HttpResponse notFound(ContentType contentType, String body) {
        return withStatus(HttpStatusCode.NOT_FOUND)
                .setBody(contentType, body);
    }

    /**
     * Creates a new HTTP response with the status code 400 (Bad Request) and the specified body.
     *
     * @param contentType the content type
     * @param body        the body as a string
     * @return a new HttpResponse instance
     */
    static HttpResponse badRequest(ContentType contentType, String body) {
        return withStatus(HttpStatusCode.BAD_REQUEST)
                .setBody(contentType, body);
    }

    /**
     * Creates a new HTTP response with the status code 500 (Internal Server Error) and the specified body.
     *
     * @param contentType the content type
     * @param body        the body as a string
     * @return a new HttpResponse instance
     */
    static HttpResponse unexpectedError(ContentType contentType, String body) {
        return withStatus(HttpStatusCode.INTERNAL_SERVER_ERROR)
                .setBody(contentType, body);
    }
}

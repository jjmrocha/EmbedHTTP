package net.uiqui.embedhttp.api;

/**
 * Enum representing HTTP status codes and their associated reason phrases.
 */
public enum HttpStatusCode {
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, "Not Modified"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    private final int code;
    private final String reasonPhrase;

    HttpStatusCode(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Enum representing HTTP status codes and their associated reason phrases.
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the reason phrase associated with the HTTP status code.
     *
     * @return the reason phrase
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}

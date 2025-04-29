package net.uiqui.embedhttp.api;

/**
 * Enum representing various content types for HTTP responses.
 * This enum can be used to specify the content type of the response in an HTTP server.
 */
public enum ContentType {
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the content type.
     *
     * @return the string value of the content type
     */
    public String getValue() {
        return value;
    }
}

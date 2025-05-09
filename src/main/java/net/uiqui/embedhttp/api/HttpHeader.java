package net.uiqui.embedhttp.api;

/**
 * Enum representing common HTTP headers.
 * Provides a mapping between header names and their string values.
 */
public enum HttpHeader {
    ACCEPT("Accept"),
    ACCEPT_ENCODING("Accept-Encoding"),
    AUTHORIZATION("Authorization"),
    CACHE_CONTROL("Cache-Control"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_TYPE("Content-Type"),
    COOKIE("Cookie"),
    DATE("Date"),
    HOST("Host"),
    LOCATION("Location"),
    ORIGIN("Origin"),
    REFERER("Referer"),
    USER_AGENT("User-Agent"),
    SET_COOKIE("Set-Cookie"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    CONNECTION("Connection");

    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the HTTP header.
     *
     * @return the string value of the header
     */
    public String getValue() {
        return value;
    }
}

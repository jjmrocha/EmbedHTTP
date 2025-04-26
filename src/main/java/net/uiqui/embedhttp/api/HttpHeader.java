package net.uiqui.embedhttp.api;

public enum HttpHeader {
    ACCEPT("Accept"),
    ACCEPT_ENCODING("Accept-Encoding"),
    AUTHORIZATION("Authorization"),
    CACHE_CONTROL("Cache-Control"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_TYPE("Content-Type"),
    COOKIE("Cookie"),
    HOST("Host"),
    LOCATION("Location"),
    ORIGIN("Origin"),
    REFERER("Referer"),
    USER_AGENT("User-Agent"),
    SET_COOKIE("Set-Cookie"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    CONNECTION("Connection"),
    UPGRADE_INSECURE_REQUESTS("Upgrade-Insecure-Requests");

    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

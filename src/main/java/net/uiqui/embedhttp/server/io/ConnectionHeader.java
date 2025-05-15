package net.uiqui.embedhttp.server.io;

public enum ConnectionHeader {
    KEEP_ALIVE("keep-alive"),
    CLOSE("close");

    private final String value;

    ConnectionHeader(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

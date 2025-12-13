package net.uiqui.embedhttp.server.io;

public enum HTTPVersion {
    VERSION_1_0("HTTP/1.0"),
    VERSION_1_1("HTTP/1.1");

    private final String value;

    HTTPVersion(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isSupported(String version) {
        return VERSION_1_0.value.equals(version) || VERSION_1_1.value.equals(version);
    }
}

package net.uiqui.embedhttp.server.io;

import java.util.HashMap;
import java.util.Map;

public enum HttpVersion {
    VERSION_1_0("HTTP/1.0"),
    VERSION_1_1("HTTP/1.1");

    private final String value;

    HttpVersion(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, HttpVersion> VERSION_MAP = HashMap.newHashMap(HttpVersion.values().length);

    static {
        for (HttpVersion version : HttpVersion.values()) {
            VERSION_MAP.put(version.getValue(), version);
        }
    }

    public static HttpVersion fromString(String version) {
        if (version == null || version.isEmpty()) {
            return null;
        }

        return VERSION_MAP.get(version);
    }
}

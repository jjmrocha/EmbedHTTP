package net.uiqui.embedhttp.server;

import java.util.Map;
import java.util.TreeMap;

public class InsensitiveMap extends TreeMap<String, String> {
    public InsensitiveMap() {
        super(String.CASE_INSENSITIVE_ORDER);
    }

    public static InsensitiveMap from(Map<String, String> map) {
        var insensitiveMap = new InsensitiveMap();
        insensitiveMap.putAll(map);
        return insensitiveMap;
    }
}

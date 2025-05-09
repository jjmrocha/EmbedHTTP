package net.uiqui.embedhttp.server;

import java.time.ZonedDateTime;

public class Now {
    private Now() {
        // Prevent instantiation
    }

    public static ZonedDateTime asZonedDateTime() {
        return ZonedDateTime.now();
    }
}

package net.uiqui.embedhttp.server;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DateHeader {
    private static final String RFC_1123_DATE_TIME = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String GMT = "GMT";

    private static final DateTimeFormatter DATE_HEADER_FORMAT = DateTimeFormatter.ofPattern(RFC_1123_DATE_TIME, Locale.ENGLISH)
            .withZone(ZoneId.of(GMT));
    private static final ZonedDateTime EPOCH = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of(GMT));

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private FormatedDate lastFormattedDate = new FormatedDate(EPOCH, "");

    public String getDateHeaderValue() {
        var now = Now.asZonedDateTime().truncatedTo(ChronoUnit.SECONDS);

        var currentFormattedDate = getCurrentFormattedDate();
        if (now.equals(lastFormattedDate.time())) {
            return currentFormattedDate.formatted();
        }

        return computeAndReturn(now);
    }

    private FormatedDate getCurrentFormattedDate() {
        readLock.lock();
        try {
            return lastFormattedDate;
        } finally {
            readLock.unlock();
        }
    }

    private String computeAndReturn(ZonedDateTime time) {
        writeLock.lock();
        try {
            if (lastFormattedDate.time().equals(time)) {
                return lastFormattedDate.formatted();
            }

            var formatted = formatDate(time);
            lastFormattedDate = new FormatedDate(time, formatted);
            return formatted;
        } finally {
            writeLock.unlock();
        }
    }

    protected String formatDate(ZonedDateTime date) {
        return DATE_HEADER_FORMAT.format(date);
    }

    private record FormatedDate(ZonedDateTime time, String formatted) {
    }
}
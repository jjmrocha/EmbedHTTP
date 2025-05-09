package net.uiqui.embedhttp.server;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DateHeader {
    private static final String RFC_1123_DATE_TIME = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String GMT = "GMT";

    private static final DateTimeFormatter DATE_HEADER_FORMAT = DateTimeFormatter.ofPattern(RFC_1123_DATE_TIME, Locale.ENGLISH)
            .withZone(ZoneId.of(GMT));

    private final LastDateHolder cache = new LastDateHolder();

    public String getDateHeaderValue() {
        var now = Now.asZonedDateTime().truncatedTo(ChronoUnit.SECONDS);
        var lastFormatedDate = cache.get();

        if (lastFormatedDate == null || !now.equals(lastFormatedDate.time)) {
            var formattedDate = formatDate(now);
            cache.set(new FormatedDate(now, formattedDate));
            return formattedDate;
        }

        return lastFormatedDate.formatted;
    }

    protected String formatDate(ZonedDateTime date) {
        return DATE_HEADER_FORMAT.format(date);
    }

    private record FormatedDate(ZonedDateTime time, String formatted) {
    }

    private static class LastDateHolder {
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        private FormatedDate lastFormatedDate = null;

        public FormatedDate get() {
            readLock.lock();
            try {
                return lastFormatedDate;
            } finally {
                readLock.unlock();
            }
        }

        public void set(FormatedDate formatedDate) {
            writeLock.lock();
            try {
                lastFormatedDate = formatedDate;
            } finally {
                writeLock.unlock();
            }
        }
    }
}
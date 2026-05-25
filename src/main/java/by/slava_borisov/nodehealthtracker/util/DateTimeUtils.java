package by.slava_borisov.nodehealthtracker.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter HUMAN_READABLE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private DateTimeUtils() {
    }

    public static String formatMoscowDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "—";
        }

        return dateTime.format(HUMAN_READABLE_DATE_TIME_FORMATTER) + " МСК";
    }
}
package fi.iki.murgo.iltasoitto.app;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeHelper {
    public static long getNextTime(int hourOfDay, int minuteOfHour, int secondOfMinute) {
        return getNextTime(LocalDateTime.now(), hourOfDay, minuteOfHour, secondOfMinute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    }

    public static LocalDateTime getNextTime(LocalDateTime now, int hourOfDay, int minuteOfHour, int secondOfMinute) {
        LocalDateTime then = now
            .withHour(hourOfDay)
            .withMinute(minuteOfHour)
            .withSecond(secondOfMinute)
            .withNano(0);
        return then.isBefore(now) ? then.plusDays(1) : then;
    }

    public static int secondsBetween(long now, long nextTime) {
        return (int) ((nextTime - now) / 1000L);
    }

    public static Duration durationUntilNextTime(int hourOfDay, int minuteOfHour, int secondOfMinute) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, getNextTime(now, hourOfDay, minuteOfHour, secondOfMinute));
    }
}

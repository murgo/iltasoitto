package fi.iki.murgo.iltasoitto.app;

import org.joda.time.DateTime;
import org.joda.time.Period;

public class TimeHelper {
    public static long getNextTime(int hourOfDay, int minuteOfHour, int secondOfMinute)
    {
        return getNextTime(new DateTime(), hourOfDay, minuteOfHour, secondOfMinute).toInstant().getMillis();
    }

    public static DateTime getNextTime(DateTime now, int hourOfDay, int minuteOfHour, int secondOfMinute) {
        DateTime then = now
                .withHourOfDay(hourOfDay)
                .withMinuteOfHour(minuteOfHour)
                .withSecondOfMinute(secondOfMinute)
                .withMillisOfSecond(0);

        return (then.isBefore(now) ? then.plusDays(1) : then);
    }

    public static int secondsBetween(long now, long nextTime) {
        return (int) ((nextTime - now) / 1000L);
    }

    public static Period periodBetween(DateTime now, DateTime then) {
        return new Period(now, then);
    }
}

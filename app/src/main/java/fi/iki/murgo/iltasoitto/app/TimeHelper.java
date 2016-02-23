package fi.iki.murgo.iltasoitto.app;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

public class TimeHelper {
    public static long getNextTime(int hourOfDay, int minuteOfHour)
    {
        return getNextTime(new DateTime(), hourOfDay, minuteOfHour);
    }

    public static long getNextTime(DateTime now, int hourOfDay, int minuteOfHour) {
        DateTime then = now
                .withHourOfDay(hourOfDay)
                .withMinuteOfHour(minuteOfHour)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        then = (then.isBefore(now) ? then.plusDays(1) : then);
        while (then.getDayOfWeek() == DateTimeConstants.SATURDAY || then.getDayOfWeek() == DateTimeConstants.SUNDAY) {
            then = then.plusDays(1);
        }

        return then.toDateTime().toInstant().getMillis();
    }

    public static int secondsBetween(long now, long nextTime) {
        return (int) ((nextTime - now) / 1000L);
    }
}

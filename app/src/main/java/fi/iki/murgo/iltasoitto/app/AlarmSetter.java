package fi.iki.murgo.iltasoitto.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmSetter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        checkAlarm(context);
    }

    private static PendingIntent createIntent(Context ctx) {
        Intent intent = new Intent(ctx, HarjuLauncher.class);
        return PendingIntent.getBroadcast(ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void clearAlarm(Context ctx) {
        PendingIntent intent = createIntent(ctx);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(intent);
        intent.cancel();
        PreferenceHelper.get(ctx).clearScheduledTime();
    }

    public static void setAlarm(Context ctx, int hour, int minute, int second) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        long triggerAt = TimeHelper.getNextTime(hour, minute, second);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // SCHEDULE_EXACT_ALARM not granted by user (API 31-32); USE_EXACT_ALARM covers API 33+
            Log.w(HarjuMainActivity.LOG_TAG, "Exact alarm permission not granted, using inexact alarm.");
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, createIntent(ctx));
            PreferenceHelper.get(ctx).saveScheduledTime(triggerAt);
            return;
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, createIntent(ctx));
        PreferenceHelper.get(ctx).saveScheduledTime(triggerAt);
    }

    public static void checkAlarm(Context ctx) {
        if (PreferenceHelper.get(ctx).isActive()) {
            int hour = PreferenceHelper.get(ctx).getHour();
            int minute = PreferenceHelper.get(ctx).getMinute();
            setAlarm(ctx, hour, minute, 0);

            int seconds = TimeHelper.secondsBetween(System.currentTimeMillis(), TimeHelper.getNextTime(hour, minute, 0));
            Log.i(HarjuMainActivity.LOG_TAG, "Iltasoitto scheduled to play in " + seconds + " seconds.");
        } else {
            clearAlarm(ctx);
            Log.i(HarjuMainActivity.LOG_TAG, "Iltasoitto cleared");
        }
    }
}

package fi.iki.murgo.iltasoitto.app;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import net.danlew.android.joda.JodaTimeAndroid;

public class AlarmSetter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        checkAlarm(context);
    }

    private static PendingIntent createIntent(Context ctx) {
        Intent intent = new Intent(ctx, HarjuLauncher.class);
        return PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void clearAlarm(Context ctx) {
        PendingIntent intent = createIntent(ctx);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(intent);
        intent.cancel();
    }

    @SuppressLint("NewApi")
    public static void setAlarm(Context ctx, int hour, int minute, int second) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        JodaTimeAndroid.init(ctx);

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, TimeHelper.getNextTime(hour, minute, second), createIntent(ctx));
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, TimeHelper.getNextTime(hour, minute, second), createIntent(ctx));
        }
    }

    public static void checkAlarm(Context ctx) {
        if (PreferenceHelper.get(ctx).isActive()) {

            int hour = PreferenceHelper.get(ctx).getHour();
            int minute = PreferenceHelper.get(ctx).getMinute();
            setAlarm(ctx, hour, minute, 0);

            int seconds = TimeHelper.secondsBetween(System.currentTimeMillis(), (TimeHelper.getNextTime(hour, minute, 0)));
            Log.i(HarjuMainActivity.LOG_TAG, "Iltasoitto scheduled to play in " + seconds + " seconds.");
        } else {
            clearAlarm(ctx);
            Log.i(HarjuMainActivity.LOG_TAG, "Iltasoitto cleared");
        }
    }
}

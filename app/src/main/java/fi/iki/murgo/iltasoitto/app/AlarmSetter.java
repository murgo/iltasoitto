package fi.iki.murgo.iltasoitto.app;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
    private static void setAlarm(Context ctx) {
        int hour = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(ctx).getString(MainActivity.KEY_PREF_HOUR, "20"));
        int minute = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(ctx).getString(MainActivity.KEY_PREF_MINUTE, "0"));

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, TimeHelper.getNextTime(hour, minute), createIntent(ctx));
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, TimeHelper.getNextTime(hour, minute), createIntent(ctx));
        }
    }

    public static void checkAlarm(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean active = prefs.getBoolean(MainActivity.KEY_PREF_ACTIVE, true);

        /*
        String activeText = ctx.getString((active ? R.string.toast_active : R.string.toast_inactive));
        Toast toast = Toast.makeText(ctx, ctx.getText(R.string.app_name) + " " + activeText + ".", Toast.LENGTH_SHORT);
        toast.show();
        */

        if (active) {
            setAlarm(ctx);

            int hour = Integer.parseInt(prefs.getString(MainActivity.KEY_PREF_HOUR, "12"));
            int minute = Integer.parseInt(prefs.getString(MainActivity.KEY_PREF_MINUTE, "30"));
            int seconds = TimeHelper.secondsBetween(System.currentTimeMillis(), (TimeHelper.getNextTime(hour, minute)));
            Log.i(MainActivity.LOG_TAG, "Iltasoitto scheduled to play in " + seconds + " seconds.");
        } else {
            clearAlarm(ctx);
            Log.i(MainActivity.LOG_TAG, "Iltasoitto cleared");
        }
    }
}

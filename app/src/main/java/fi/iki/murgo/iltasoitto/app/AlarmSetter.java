package fi.iki.murgo.iltasoitto.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmSetter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        checkAlarm(context);
    }

    private static long getMillisecondsUntilEight() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        if (c.getTimeInMillis() - 60000 < System.currentTimeMillis()) {
            c.add(Calendar.DATE, 1);
        }

        return c.getTimeInMillis();
    }

    private static PendingIntent createIntent(Context ctx) {
        Intent intent = new Intent(ctx, HarjuPlayer.class);
        return PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void clearAlarm(Context ctx) {
        PendingIntent intent = createIntent(ctx);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(intent);
        intent.cancel();
    }

    public static void setAlarm(Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getMillisecondsUntilEight(), AlarmManager.INTERVAL_DAY, createIntent(ctx));
    }

    public static void checkAlarm(Context ctx) {
        boolean active = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(MainActivity.KEY_PREF_ACTIVE, true);

        /*
        String activeText = ctx.getString((active ? R.string.toast_active : R.string.toast_inactive));
        Toast toast = Toast.makeText(ctx, ctx.getText(R.string.app_name) + " " + activeText + ".", Toast.LENGTH_SHORT);
        toast.show();
        */

        if (active) {
            setAlarm(ctx);
        } else {
            clearAlarm(ctx);
        }
    }
}

package fi.iki.murgo.iltasoitto.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class AlarmSetter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        checkAlarm(context);
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
        int hour = PreferenceManager.getDefaultSharedPreferences(ctx).getInt(MainActivity.KEY_PREF_HOUR, 20);
        int minute = PreferenceManager.getDefaultSharedPreferences(ctx).getInt(MainActivity.KEY_PREF_MINUTE, 0);

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, TimeHelper.getNextTime(hour, minute) , createIntent(ctx));
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

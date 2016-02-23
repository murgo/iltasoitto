package fi.iki.murgo.iltasoitto.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class MainActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_ACTIVE = "pref_active";
    public static final String KEY_PREF_HOUR = "pref_hour";
    public static final String KEY_PREF_MINUTE = "pref_minute";
    public static final String LOG_TAG = "iltasoitto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        AlarmSetter.checkAlarm(this);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.remove(KEY_PREF_HOUR);
        editor.remove(KEY_PREF_MINUTE);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_ACTIVE)) {
            AlarmSetter.checkAlarm(this);

            //showToast();
        }
    }

    private void showToast() {
        // DEBUG
        boolean active = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MainActivity.KEY_PREF_ACTIVE, true);
        String activeText = getString((active ? R.string.toast_active : R.string.toast_inactive));
        Toast toast;
        if (active) {
            int hour = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(MainActivity.KEY_PREF_HOUR, "12"));
            int minute = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(MainActivity.KEY_PREF_MINUTE, "30"));
            int seconds = TimeHelper.secondsBetween(System.currentTimeMillis(), (TimeHelper.getNextTime(hour, minute)));
            toast = Toast.makeText(this, getText(R.string.app_name) + " " + getText(R.string.toast_active) + ". Seconds until next play: " + seconds, Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(this, getText(R.string.app_name) + " " + activeText + ".", Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}

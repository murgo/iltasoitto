package fi.iki.murgo.iltasoitto.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_ACTIVE = "pref_active";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        AlarmSetter.checkAlarm(this);
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
        }
    }
}

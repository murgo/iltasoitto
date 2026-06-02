package fi.iki.murgo.iltasoitto.app;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    public static final String KEY_PREF_ACTIVE = "pref_active";
    public static final String KEY_PREF_HOUR = "pref_hour";
    public static final String KEY_PREF_MINUTE = "pref_minute";

    private static PreferenceHelper _instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PreferenceHelper(Context ctx) {
        // Use same file name as PreferenceManager.getDefaultSharedPreferences() for data compatibility
        prefs = ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static PreferenceHelper get(Context ctx) {
        if (_instance == null) _instance = new PreferenceHelper(ctx);
        return _instance;
    }

    public boolean isActive() {
        return prefs.getBoolean(KEY_PREF_ACTIVE, true);
    }

    public void clean() {
        editor.remove(KEY_PREF_HOUR);
        editor.remove(KEY_PREF_MINUTE);
        editor.apply();
    }

    public void save(boolean isChecked) {
        editor.putBoolean(KEY_PREF_ACTIVE, isChecked);
        editor.apply();
    }

    public int getHour() {
        return Integer.parseInt(prefs.getString(KEY_PREF_HOUR, "20"));
    }

    public int getMinute() {
        return Integer.parseInt(prefs.getString(KEY_PREF_MINUTE, "0"));
    }
}

package fi.iki.murgo.iltasoitto.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

// WakefulBroadcastReceiver was removed in API 31. ContextCompat.startForegroundService
// gives Android 5s to call startForeground(), which MusicPlayerService does immediately
// in onStartCommand, so no external wake lock is needed.
public class HarjuLauncher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MusicPlayerService.class);
        ContextCompat.startForegroundService(context, service);
    }
}

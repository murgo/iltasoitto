package fi.iki.murgo.iltasoitto.app;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class HarjuLauncher extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MusicPlayerService.class);
        startWakefulService(context, service);
    }
}

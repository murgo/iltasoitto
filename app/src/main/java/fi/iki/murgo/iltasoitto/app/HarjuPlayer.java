package fi.iki.murgo.iltasoitto.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

public class HarjuPlayer extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        mediaPlayer = MediaPlayer.create(context, R.raw.iltasoitto);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("iltasoitto", "Iltasoitto played succesfully.");
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();

        AlarmSetter.checkAlarm(context);
    }
}

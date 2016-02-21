package fi.iki.murgo.iltasoitto.app;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

public class MusicPlayerService extends IntentService {
    private static MediaPlayer mediaPlayer;

    public MusicPlayerService() {
        super("MusicPlayerService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Context context = getApplicationContext();

        mediaPlayer = MediaPlayer.create(context, R.raw.sonni);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("iltasonni", "Iltasonni played succesfully.");
                mediaPlayer.release();
                mediaPlayer = null;

                HarjuLauncher.completeWakefulIntent(intent);
            }
        });
        mediaPlayer.start();

        AlarmSetter.checkAlarm(context);
    }
}

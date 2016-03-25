package fi.iki.murgo.iltasoitto.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MusicPlayerService extends Service {
    private static final int NOTIFICATION_ID = 0x666;
    private static final String STARTED = "STARTED";
    private MediaPlayer mediaPlayer;
    private Intent startIntent;
    private Thread backgroundThread;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = MediaPlayer.create(this, R.raw.iltasoitto);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(HarjuMainActivity.LOG_TAG, "Iltasoitto played succesfully.");

                stopSelf();
            }
        });

        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(HarjuMainActivity.LOG_TAG, "Starting player.");
                mediaPlayer.start();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (backgroundThread != null) {
            Thread dummy = backgroundThread;
            backgroundThread = null;
            dummy.interrupt();
        }

        if (startIntent != null) {
            HarjuLauncher.completeWakefulIntent(startIntent);
            startIntent = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(STARTED, true)) {
            startIntent = intent;

            startForeground(NOTIFICATION_ID, getNotification());

            if (!mediaPlayer.isPlaying()) {
                backgroundThread.start();
            }

            AlarmSetter.checkAlarm(this);
        } else {
            stopSelf();
        }

        return START_STICKY;
    }

    private Notification getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(getText(R.string.notification_title)).setContentText(getText(R.string.notification_text)).setWhen(System.currentTimeMillis());
        Intent service = new Intent(this, MusicPlayerService.class);
        service.putExtra(STARTED, false);
        PendingIntent pi = PendingIntent.getService(this, 0, service, 0);

        builder.setContentIntent(pi);

        return builder.build();
    }
}

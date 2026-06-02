package fi.iki.murgo.iltasoitto.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MusicPlayerService extends Service {
    private static final int NOTIFICATION_ID = 0x666;
    private static final String CHANNEL_ID = "iltasoitto_channel";
    private static final String STARTED = "STARTED";
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        mediaPlayer = MediaPlayer.create(this, R.raw.iltasoitto);
        mediaPlayer.setOnCompletionListener(mp -> {
            Log.i(HarjuMainActivity.LOG_TAG, "Iltasoitto played successfully.");
            stopSelf();
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getBooleanExtra(STARTED, true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, getNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }

            if (!mediaPlayer.isPlaying()) {
                Log.i(HarjuMainActivity.LOG_TAG, "Starting player.");
                mediaPlayer.start();
            }

            AlarmSetter.checkAlarm(this);
        } else {
            stopSelf();
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    private Notification getNotification() {
        Intent service = new Intent(this, MusicPlayerService.class);
        service.putExtra(STARTED, false);
        PendingIntent pi = PendingIntent.getService(this, 0, service,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_text))
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pi)
            .build();
    }
}

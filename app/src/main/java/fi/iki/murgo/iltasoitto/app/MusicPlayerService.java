package fi.iki.murgo.iltasoitto.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ServiceCompat;

public class MusicPlayerService extends Service {
    private static final int NOTIFICATION_ID = 0x666;
    private static final String CHANNEL_ID = "iltasoitto_channel";
    private static final String STARTED = "STARTED";
    public static final String ALARM_TRIGGERED = "ALARM_TRIGGERED";
    public interface PlaybackListener {
        void onPlaybackStopped();
    }

    public static boolean isPlaying = false;
    private static MusicPlayerService instance;
    private static PlaybackListener playbackListener;
    private MediaPlayer mediaPlayer;

    public static void setPlaybackListener(PlaybackListener listener) {
        playbackListener = listener;
    }

    public static void applyVolume(int sliderProgress) {
        if (instance != null && instance.mediaPlayer != null) {
            float volume = sliderToAmplitude(sliderProgress);
            instance.mediaPlayer.setVolume(volume, volume);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createNotificationChannel();

        mediaPlayer = MediaPlayer.create(this, R.raw.iltasoitto);
        float volume = sliderToAmplitude(PreferenceHelper.get(this).getVolume());
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.setOnCompletionListener(mp -> {
            Log.i(HarjuMainActivity.LOG_TAG, "Iltasoitto played successfully.");
            stopSelf();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPlaying = false;
        instance = null;
        if (playbackListener != null) playbackListener.onPlaybackStopped();
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean alarmTriggered = intent != null && intent.getBooleanExtra(ALARM_TRIGGERED, false);

        // Read before rescheduling so the tolerance check below sees the time that just fired.
        long scheduledTime = alarmTriggered ? PreferenceHelper.get(this).getScheduledTime() : -1;

        if (alarmTriggered) {
            // Always schedule for the next calendar day to prevent a second play on the same day
            // when the alarm fires slightly before the target time.
            AlarmSetter.reschedule(this);
        } else {
            AlarmSetter.checkAlarm(this);
        }

        if (intent == null || intent.getBooleanExtra(STARTED, true)) {
            // startForeground must be called before any early return on API 31+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, getNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }

            // Guard against extreme drift from inexact alarms or clock corrections.
            if (scheduledTime > 0) {
                long driftMs = Math.abs(System.currentTimeMillis() - scheduledTime);
                if (driftMs > 10 * 60 * 1000L) {
                    Log.w(HarjuMainActivity.LOG_TAG, "Alarm fired " + driftMs / 1000 + "s from scheduled time, skipping playback.");
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }

            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                Log.i(HarjuMainActivity.LOG_TAG, "Notifications disabled, skipping playback (user cannot cancel).");
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
                stopSelf();
                return START_NOT_STICKY;
            }

            AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audio.getRingerMode() != AudioManager.RINGER_MODE_NORMAL
                    && !PreferenceHelper.get(this).getOverrideSilent()) {
                Log.i(HarjuMainActivity.LOG_TAG, "Phone is on silent/vibrate, skipping playback.");
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
                stopSelf();
                return START_NOT_STICKY;
            }

            int audioMode = audio.getMode();
            if (audioMode == AudioManager.MODE_IN_CALL || audioMode == AudioManager.MODE_IN_COMMUNICATION) {
                Log.i(HarjuMainActivity.LOG_TAG, "Phone call in progress, skipping playback.");
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
                stopSelf();
                return START_NOT_STICKY;
            }

            if (!mediaPlayer.isPlaying()) {
                Log.i(HarjuMainActivity.LOG_TAG, "Starting player.");
                mediaPlayer.start();
                isPlaying = true;
            }
        } else {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    // Converts a linear slider value (0–100) to an amplitude (0.0–1.0) using a dB curve
    // so that 50% on the slider produces ~50% perceived loudness (≈ −10 dB).
    private static float sliderToAmplitude(int progress) {
        if (progress <= 0) return 0.0f;
        if (progress >= 100) return 1.0f;
        float dB = 20.0f * (progress / 100.0f - 1.0f); // maps 0–100 to −20 dB … 0 dB
        return (float) Math.pow(10.0, dB / 20.0);
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
            .setDeleteIntent(pi)
            .build();
    }
}

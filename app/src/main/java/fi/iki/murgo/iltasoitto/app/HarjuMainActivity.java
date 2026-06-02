package fi.iki.murgo.iltasoitto.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.Duration;

public class HarjuMainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "Harjun Iltasoitto";
    private LogoAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_harju_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }

        boolean active = PreferenceHelper.get(this).isActive();
        findViewById(R.id.logo).setVisibility(active ? View.VISIBLE : View.INVISIBLE);
        ((ToggleButton) findViewById(R.id.harjuToggle)).setChecked(active);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.harjuToggle);
        toggle.setOnCheckedChangeListener(getStateListener());
        View logo = findViewById(R.id.logo);
        logo.setOnClickListener(getCreditsListener());

        PreferenceHelper.get(this).clean();
        AlarmSetter.checkAlarm(this);

        SeekBar volumeSlider = findViewById(R.id.volume_slider);
        volumeSlider.setProgress(PreferenceHelper.get(this).getVolume());
        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    PreferenceHelper.get(HarjuMainActivity.this).saveVolume(progress);
                    MusicPlayerService.applyVolume(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.play_button).setOnClickListener(v -> togglePlayback());
        updatePlayButton();

        findViewById(R.id.notification_warning_card).setOnClickListener(v -> openNotificationSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean enabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        findViewById(R.id.notification_warning_card).setVisibility(enabled ? View.GONE : View.VISIBLE);
        updatePlayButton();
        MusicPlayerService.setPlaybackListener(() -> updatePlayButton());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicPlayerService.setPlaybackListener(null);
    }

    private void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        }
        startActivity(intent);
    }

    private void togglePlayback() {
        if (MusicPlayerService.isPlaying) {
            stopService(new Intent(this, MusicPlayerService.class));
            updatePlayButton(false);
        } else {
            ContextCompat.startForegroundService(this, new Intent(this, MusicPlayerService.class));
            updatePlayButton(true);
        }
    }

    private void updatePlayButton() {
        updatePlayButton(MusicPlayerService.isPlaying);
    }

    private void updatePlayButton(boolean playing) {
        ((Button) findViewById(R.id.play_button))
            .setText(playing ? R.string.play_button_stop : R.string.play_button_play);
    }

    private View.OnClickListener getCreditsListener() {
        return v -> {
            if (!PreferenceHelper.get(HarjuMainActivity.this).isActive()) return;
            togglePlayback();
        };
    }

    private CompoundButton.OnCheckedChangeListener getStateListener() {
        return (buttonView, isChecked) -> {
            PreferenceHelper.get(HarjuMainActivity.this).save(isChecked);
            AlarmSetter.checkAlarm(HarjuMainActivity.this);
            showToast();
            animate(isChecked);
        };
    }

    private void animate(boolean show) {
        if (animator == null)
            animator = new LogoAnimator();
        if (show)
            animator.show(this);
        else
            animator.hide(this);
    }

    private void showToast() {
        if (PreferenceHelper.get(this).isActive()) {
            int hour = PreferenceHelper.get(this).getHour();
            int minute = PreferenceHelper.get(this).getMinute();
            Duration between = TimeHelper.durationUntilNextTime(hour, minute, 0);
            long hours = between.toHours();
            long minutes = between.toMinutes() % 60;
            Toast.makeText(this,
                getString(R.string.nextplay) + hours + ":" + String.format("%02d", minutes),
                Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.toast_inactive), Toast.LENGTH_SHORT).show();
        }
    }
}

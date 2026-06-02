package fi.iki.murgo.iltasoitto.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.Duration;
import java.time.LocalDateTime;

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
    }

    private View.OnClickListener getCreditsListener() {
        return v -> {
            if (PreferenceHelper.get(HarjuMainActivity.this).isActive()) {
                LocalDateTime date = LocalDateTime.now().plusSeconds(1);
                AlarmSetter.setAlarm(HarjuMainActivity.this, date.getHour(), date.getMinute(), date.getSecond());
            }
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
        }
    }
}

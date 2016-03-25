package fi.iki.murgo.iltasoitto.app;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class HarjuMainActivity extends Activity {
    public static final String LOG_TAG = "Harjun Iltasoitto";
    private LogoAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harju_main);

        boolean active = PreferenceHelper.get(this).isActive();
        findViewById(R.id.logo).setVisibility(active ? View.VISIBLE :  View.INVISIBLE);
        ((ToggleButton) findViewById(R.id.harjuToggle)).setChecked(active);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.harjuToggle);
        toggle.setOnCheckedChangeListener(getStateListener());
        View logo = findViewById(R.id.logo);
        logo.setOnClickListener(getCreditsListener());

        PreferenceHelper.get(this).clean();
        AlarmSetter.checkAlarm(this);
    }

    private View.OnClickListener getCreditsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferenceHelper.get(HarjuMainActivity.this).isActive()) {
                    DateTime date = new DateTime().plusSeconds(1);
                    AlarmSetter.setAlarm(HarjuMainActivity.this, date.getHourOfDay(), date.getMinuteOfHour(), date.getSecondOfMinute());
                }
            }
        };
    }

    private CompoundButton.OnCheckedChangeListener getStateListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceHelper.get(HarjuMainActivity.this).save(isChecked);
                AlarmSetter.checkAlarm(HarjuMainActivity.this);
                showToast();
                animate(isChecked);
            }
        };
    }

    private void animate(boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (animator == null)
                animator = new LogoAnimator();

            if (show)
                animator.show(this);
            else
                animator.hide(this);
        } else {
            findViewById(R.id.logo).setVisibility(show ? View.VISIBLE :  View.INVISIBLE);
        }
    }

    private void showToast() {
        if (PreferenceHelper.get(this).isActive()) {
            int hour = PreferenceHelper.get(this).getHour();
            int minute = PreferenceHelper.get(this).getMinute();
            Period between = TimeHelper.periodBetween(new DateTime(), TimeHelper.getNextTime(new DateTime(), hour, minute, 0));
            Toast toast = Toast.makeText(this, getString(R.string.nextplay) + between.getHours() + ":" + String.format("%02d", between.getMinutes()), Toast.LENGTH_LONG);
            toast.show();
        }
    }
}

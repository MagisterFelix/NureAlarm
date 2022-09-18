package com.nure.alarm.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;

public class AlarmActivity extends AppCompatActivity {

    private final static String ROTATION_ANIMATION = "rotation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        if (((PowerManager) getSystemService(Context.POWER_SERVICE)).isInteractive()) {
            finish();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ImageView alarmClock = findViewById(R.id.alarm_clock);
        alarmClock.setOnClickListener(view -> {
            Alarm.stopAlarm(getApplicationContext());
            AlarmClockActivity.updateActivity(getApplicationContext());
            finish();
        });

        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(alarmClock, ROTATION_ANIMATION, 0f, 20f, 0f, -20f, 0f);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimation.setDuration(800);
        rotateAnimation.start();
    }
}
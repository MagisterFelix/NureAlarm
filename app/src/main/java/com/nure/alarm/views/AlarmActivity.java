package com.nure.alarm.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.managers.ContextManager;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.Information;

import org.json.JSONException;

public class AlarmActivity extends AppCompatActivity {

    private static final String ROTATION_ANIMATION = "rotation";

    private Runnable runnable;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Information information = FileManager.readInfo(getApplicationContext());

        if (((PowerManager) getSystemService(Context.POWER_SERVICE)).isInteractive()) {
            finish();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ImageView alarmClock = findViewById(R.id.alarm_clock);
        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(alarmClock, ROTATION_ANIMATION, 0f, 20f, 0f, -20f, 0f);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimation.setDuration(800);
        rotateAnimation.start();

        TextView alarmLessonTime = findViewById(R.id.alarm_lesson_time);
        try {
            String text = information.getAlarm().getString("name") + " - " + information.getAlarm().getString("time");
            alarmLessonTime.setText(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ImageView firstCircle = findViewById(R.id.first_circle);
        ImageView secondCircle = findViewById(R.id.second_circle);

        runnable = () -> {
            firstCircle.animate().scaleX(3f).scaleY(3f).alpha(0f).setDuration(1000).withEndAction(() -> {
                firstCircle.setScaleX(1f);
                firstCircle.setScaleY(1f);
                firstCircle.setAlpha(1f);
            });

            secondCircle.animate().scaleX(3f).scaleY(3f).alpha(0f).setDuration(700).withEndAction(() -> {
                secondCircle.setScaleX(1f);
                secondCircle.setScaleY(1f);
                secondCircle.setAlpha(1f);
            });

            handler.postDelayed(runnable, 1500);
        };
        runnable.run();

        ImageButton dismissAlarmButton = findViewById(R.id.dismiss_alarm_button);
        dismissAlarmButton.setOnClickListener(view -> {
            handler.removeCallbacks(runnable);

            Alarm.stopAlarm(getApplicationContext());
            AlarmClockActivity.updateActivity(getApplicationContext(), false);
            finish();
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ContextManager.getLocaleContext(base));
    }
}
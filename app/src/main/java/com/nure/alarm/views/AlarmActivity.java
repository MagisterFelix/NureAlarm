package com.nure.alarm.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.managers.ContextManager;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.models.LessonsType;
import com.nure.alarm.core.work.AlarmWorkerReceiver;

import org.json.JSONException;

public class AlarmActivity extends AppCompatActivity {

    private static final String ROTATION_ANIMATION = "rotation";
    private static final String ALPHA_ANIMATION = "alpha";
    private static final String TRANSLATION_ANIMATION = "translationX";

    private FrameLayout swipeButtonContainer;
    private ImageView swipeButton;

    private float dX;

    @SuppressLint("ClickableViewAccessibility")
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

        swipeButtonContainer = findViewById(R.id.swipe_button_container);
        swipeButton = findViewById(R.id.swipe_button);
        swipeButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX;
                    if (newX < 0) {
                        newX = 0;
                    } else if (newX + swipeButton.getWidth() > swipeButtonContainer.getWidth()) {
                        newX = swipeButtonContainer.getWidth() - swipeButton.getWidth();
                    }
                    v.setX(newX);
                    if (newX + (float) swipeButton.getWidth() / 2 < swipeButtonContainer.getWidth() * 0.5f) {
                        swipeButton.setImageResource(R.drawable.ic_alarm_off);
                    } else if (newX + (float) swipeButton.getWidth() / 2 > swipeButtonContainer.getWidth() * 0.5f) {
                        swipeButton.setImageResource(R.drawable.ic_alarm_set);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (v.getX() + (double) v.getWidth() / 2 > swipeButtonContainer.getWidth() * 0.75) {
                        reset();
                    } else if (v.getX() + (double) v.getWidth() / 2 < swipeButtonContainer.getWidth() * 0.25) {
                        dismiss();
                    } else {
                        resetButton();
                    }
                    return true;
            }
            return false;
        });

        ImageView leftArrow1, leftArrow2, rightArrow1, rightArrow2;
        leftArrow1 = findViewById(R.id.left_arrow_1);
        leftArrow2 = findViewById(R.id.left_arrow_2);
        rightArrow1 = findViewById(R.id.right_arrow_1);
        rightArrow2 = findViewById(R.id.right_arrow_2);
        setupArrowAnimation(leftArrow1, 0, -50f);
        setupArrowAnimation(leftArrow2, 800, -50f);
        setupArrowAnimation(rightArrow1, 0, 50f);
        setupArrowAnimation(rightArrow2, 800, 50f);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ContextManager.getLocaleContext(base));
    }

    private void setupArrowAnimation(ImageView arrow, long startDelay, float translation) {
        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(arrow, ALPHA_ANIMATION, 1f, 0f);
        alphaAnimation.setDuration(1600);
        alphaAnimation.setStartDelay(startDelay);
        alphaAnimation.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator translationAnimation = ObjectAnimator.ofFloat(arrow, TRANSLATION_ANIMATION, 0f, translation);
        translationAnimation.setDuration(1600);
        translationAnimation.setStartDelay(startDelay);
        translationAnimation.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimation, translationAnimation);
        animatorSet.start();
    }

    private void dismiss() {
        Alarm.stopAlarm(getApplicationContext());
        AlarmClockActivity.updateActivity(getApplicationContext(), false);
        finish();
    }

    private void reset() {
        Alarm.stopAlarm(getApplicationContext());
        AlarmClockActivity.updateActivity(getApplicationContext(), true);
        AlarmWorkerReceiver.startWork(getApplicationContext(), LessonsType.TODAY_NEAREST, true);
        finish();
    }

    private void resetButton() {
        swipeButton.animate().x((float) (swipeButtonContainer.getWidth() - swipeButton.getWidth()) / 2).setDuration(150).start();
        swipeButton.setImageResource(R.drawable.ic_stop);
    }
}
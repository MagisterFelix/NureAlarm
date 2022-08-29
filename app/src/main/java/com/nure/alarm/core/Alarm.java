package com.nure.alarm.core;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.work.AlarmWorkManager;

import java.util.Calendar;

public class Alarm {
    private final static int ZERO_SECONDS = 0;
    private final static int ONE_DAY = 1;
    private final static boolean SKIP_UI = true;

    public static void enableAlarm(Context context, Information information) {
        Calendar now = Calendar.getInstance();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, information.getSettingHour());
        startTime.set(Calendar.MINUTE, information.getSettingMinute());
        startTime.set(Calendar.SECOND, ZERO_SECONDS);

        if (startTime.before(now) || startTime.equals(now)) {
            startTime.add(Calendar.DATE, ONE_DAY);
            AlarmWorkManager.oneTimeWork(context);
        }

        long delay = startTime.getTimeInMillis() - now.getTimeInMillis();
        AlarmWorkManager.periodicWork(context, delay);
    }

    public static void setAlarm(Context context) {
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, 6);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, 0);
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, SKIP_UI);
        context.startActivity(alarmIntent);
    }

    public static void cancelAlarm(Context context) {
        Intent alarmIntent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }

    public static void disableAlarm(Context context) {
        AlarmWorkManager.cancelWork(context);
        cancelAlarm(context);
    }
}

package com.nure.alarm;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

public class Alarm {
    public static void setAlarm(Context context) {
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, 6);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, 0);
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        context.startActivity(alarmIntent);
    }

    public static void cancelAlarm(Context context) {
        Intent alarmIntent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }
}

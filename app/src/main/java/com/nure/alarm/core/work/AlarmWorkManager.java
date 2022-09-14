package com.nure.alarm.core.work;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmWorkManager {

    public static void setAlarmWork(Context context, long time) {
        PendingIntent alarmWorkManagerReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmWorkerReceiver.class).setAction("start"),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, alarmWorkManagerReceiver);
    }

    public static void cancelAlarmWork(Context context) {
        PendingIntent alarmWorkManagerReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmWorkerReceiver.class).setAction("cancel"),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmWorkManagerReceiver);
    }
}

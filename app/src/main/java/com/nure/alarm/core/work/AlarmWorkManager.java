package com.nure.alarm.core.work;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.nure.alarm.views.MainActivity;

public class AlarmWorkManager {

    public static void setAlarmWork(Context context, long time) {
        PendingIntent mainActivity = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent alarmWorkerReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmWorkerReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time, mainActivity), alarmWorkerReceiver);
    }

    public static void cancelAlarmWork(Context context) {
        PendingIntent alarmWorkerReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmWorkerReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmWorkerReceiver);

        AlarmWorkerReceiver.cancelWork(context);
    }
}

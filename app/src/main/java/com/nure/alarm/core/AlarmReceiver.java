package com.nure.alarm.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nure.alarm.core.notification.AlarmNotification;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alarmService = new Intent(context, AlarmService.class);
        context.startForegroundService(alarmService);
        AlarmNotification.cancelNotification(context, AlarmNotification.NOTIFICATION_ID);
    }
}

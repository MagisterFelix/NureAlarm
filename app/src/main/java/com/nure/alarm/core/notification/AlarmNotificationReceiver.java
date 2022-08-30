package com.nure.alarm.core.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nure.alarm.core.Alarm;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals("dismiss")) {
            Alarm.disableAlarm(context);
        }
    }
}

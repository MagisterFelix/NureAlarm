package com.nure.alarm.core.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.work.AlarmWorkerReceiver;
import com.nure.alarm.views.AlarmClockActivity;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    private final static String ACTION_DISMISS = "dismiss";
    private final static String ACTION_CHANGE = "change";
    private final static String ACTION_REMOVE = "remove";
    private final static String ACTION_RETRY = "retry";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(ACTION_DISMISS)) {
            Alarm.stopAlarm(context);
        } else {
            if (action.equals(ACTION_CHANGE)) {
                Intent alarmClockActivity = new Intent(context, AlarmClockActivity.class);
                alarmClockActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                alarmClockActivity.setAction(action);
                context.startActivity(alarmClockActivity);
            }

            if (action.equals(ACTION_REMOVE)) {
                Alarm.cancelAlarm(context);
            }

            if (action.equals(ACTION_RETRY)) {
                AlarmWorkerReceiver.startWork(context);
            }

            NotificationManagerCompat.from(context).cancel(AlarmNotification.NOTIFICATION_ID);
        }

        if (!action.equals(ACTION_CHANGE)) {
            AlarmClockActivity.updateActivity(context);
        }
    }
}

package com.nure.alarm.core.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.models.LessonsType;
import com.nure.alarm.core.work.AlarmWorkerReceiver;
import com.nure.alarm.views.AlarmClockActivity;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_DISMISS = "dismiss";
    public static final String ACTION_CHANGE = "change";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_RETRY = "retry";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(ACTION_DISMISS)) {
            Alarm.stopAlarm(context);
            AlarmClockActivity.updateActivity(context);
        } else {
            NotificationManagerCompat.from(context).cancel(AlarmNotification.NOTIFICATION_ID);

            if (action.equals(ACTION_CHANGE)) {
                Intent alarmClockActivity = new Intent(context, AlarmClockActivity.class);
                alarmClockActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alarmClockActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                alarmClockActivity.setAction(action);
                context.startActivity(alarmClockActivity);
            } else {
                if (action.equals(ACTION_REMOVE)) {
                    Alarm.cancelAlarm(context);
                }
                if (action.equals(ACTION_RETRY)) {
                    AlarmWorkerReceiver.startWork(context, intent.getIntExtra(LessonsType.class.getSimpleName(), LessonsType.AUTO));
                }

                AlarmClockActivity.updateActivity(context);
            }
        }
    }
}

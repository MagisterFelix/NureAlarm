package com.nure.alarm.core.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.nure.alarm.core.Alarm;
import com.nure.alarm.views.MainActivity;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals("change")) {
            Intent mainActivity = new Intent(context, MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            mainActivity.setAction("change");
            context.startActivity(mainActivity);
        }

        if (action.equals("dismiss")) {
            Alarm.disableAlarm(context);
        }

        NotificationManagerCompat.from(context).cancel(1);
    }
}

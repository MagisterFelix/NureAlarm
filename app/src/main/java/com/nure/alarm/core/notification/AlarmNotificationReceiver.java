package com.nure.alarm.core.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.nure.alarm.core.FileManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.views.MainActivity;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Intent mainActivity = new Intent(context, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if (action.equals("change")) {
            mainActivity.setAction("change");
        }

        if (action.equals("dismiss")) {
            Information information = FileManager.readInfo(context);
            information.setStatus(false);
            FileManager.writeInfo(context, information);
            mainActivity.setAction("dismiss");
        }

        context.startActivity(mainActivity);
        NotificationManagerCompat.from(context).cancel(1);
    }
}

package com.nure.alarm.core.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.Information;

import java.util.Objects;

public class AlarmWorkManagerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Information information = FileManager.readInfo(context);

            if (Objects.requireNonNull(information).isEnabled()) {
                Alarm.enableAlarmWork(context, information);
            }
        }
    }
}

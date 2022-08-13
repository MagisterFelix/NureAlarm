package com.nure.alarm.core.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nure.alarm.core.FileManager;
import com.nure.alarm.core.models.Information;

import java.util.Calendar;
import java.util.Objects;

public class AlarmWorkManagerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Information information = FileManager.readInfo(context);

            if (Objects.requireNonNull(information).getStatus()) {
                Calendar now = Calendar.getInstance();

                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.HOUR_OF_DAY, Objects.requireNonNull(information).getSettingHour());
                startTime.set(Calendar.MINUTE, Objects.requireNonNull(information).getSettingMinute());
                startTime.set(Calendar.SECOND, 0);

                if (startTime.before(now) || startTime.equals(now)) {
                    startTime.add(Calendar.DATE, 1);
                    AlarmWorkManager.oneTimeWork(context);
                }

                long delay = startTime.getTimeInMillis() - now.getTimeInMillis();
                AlarmWorkManager.periodicWork(context, delay);
            }
        }
    }
}

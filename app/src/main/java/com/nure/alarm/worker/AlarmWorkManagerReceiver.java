package com.nure.alarm.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.FileManager;
import com.nure.alarm.core.Information;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AlarmWorkManagerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Information information = FileManager.getInfo(context);

            if (Objects.requireNonNull(information).getStatus()) {
                Calendar now = Calendar.getInstance();

                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.HOUR_OF_DAY, Objects.requireNonNull(information).getAlarmSettingHour());
                startTime.set(Calendar.MINUTE, Objects.requireNonNull(information).getAlarmSettingMinute());
                startTime.set(Calendar.SECOND, 0);

                if (startTime.before(now) || startTime.equals(now)) {
                    startTime.add(Calendar.DATE, 1);

                    OneTimeWorkRequest request = new OneTimeWorkRequest
                            .Builder(AlarmWorker.class)
                            .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .addTag("AlarmWork")
                            .build();

                    WorkManager.getInstance(context).enqueue(request);
                }

                long delay = startTime.getTimeInMillis() - now.getTimeInMillis();

                PeriodicWorkRequest request = new PeriodicWorkRequest
                        .Builder(AlarmWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .addTag("AlarmWork")
                        .build();

                WorkManager.getInstance(context).enqueue(request);
            }
        }
    }
}

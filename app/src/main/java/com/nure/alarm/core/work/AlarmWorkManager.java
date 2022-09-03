package com.nure.alarm.core.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class AlarmWorkManager {
    private final static Class<AlarmWorker> ALARM_WORKER_CLASS = AlarmWorker.class;
    private final static Constraints NETWORK_CONNECTED = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    private final static String ALARM_WORK_TAG = "AlarmWork";

    public static void startWork(Context context, long delay) {
        OneTimeWorkRequest request = new OneTimeWorkRequest
                .Builder(ALARM_WORKER_CLASS)
                .setConstraints(NETWORK_CONNECTED)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(ALARM_WORK_TAG)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }

    public static void cancelWork(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(ALARM_WORK_TAG);
    }
}

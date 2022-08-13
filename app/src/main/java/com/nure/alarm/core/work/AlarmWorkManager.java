package com.nure.alarm.core.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class AlarmWorkManager {
    private final static Class<AlarmWorker> ALARM_WORKER_CLASS = AlarmWorker.class;
    private final static Constraints NETWORK_CONNECTED = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    private final static String ALARM_WORK_TAG = "AlarmWork";

    private final static int ONE_DAY_INTERVAL = 1;

    public static void oneTimeWork(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest
                .Builder(ALARM_WORKER_CLASS)
                .setConstraints(NETWORK_CONNECTED)
                .addTag(ALARM_WORK_TAG)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }

    public static void periodicWork(Context context, long delay) {
        PeriodicWorkRequest request = new PeriodicWorkRequest
                .Builder(ALARM_WORKER_CLASS, ONE_DAY_INTERVAL, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(NETWORK_CONNECTED)
                .addTag(ALARM_WORK_TAG)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }

    public static void cancelWork(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(ALARM_WORK_TAG);
    }
}

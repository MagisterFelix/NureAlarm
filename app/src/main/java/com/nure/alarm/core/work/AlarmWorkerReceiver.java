package com.nure.alarm.core.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.nure.alarm.core.models.LessonsType;

public class AlarmWorkerReceiver extends BroadcastReceiver {

    private static final Class<AlarmWorker> ALARM_WORKER_CLASS = AlarmWorker.class;
    private static final Constraints NETWORK_CONNECTED = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    private static final String ALARM_WORK_TAG = "AlarmWork";

    public static final String LESSONS_TYPE_KEY = "lessons_type";
    public static final String FORCE_NOTIFY_KEY = "force_notify";

    public static void startWork(Context context, int lessonsType, boolean forceNotify) {
        OneTimeWorkRequest request = new OneTimeWorkRequest
                .Builder(ALARM_WORKER_CLASS)
                .setConstraints(NETWORK_CONNECTED)
                .addTag(ALARM_WORK_TAG)
                .setInputData(new Data.Builder().putInt(LESSONS_TYPE_KEY, lessonsType).putBoolean(FORCE_NOTIFY_KEY, forceNotify).build())
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(ALARM_WORK_TAG, ExistingWorkPolicy.APPEND_OR_REPLACE, request);
    }

    public static void cancelWork(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(ALARM_WORK_TAG);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        startWork(context, LessonsType.AUTO, false);
    }
}

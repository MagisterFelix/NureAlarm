package com.nure.alarm.core.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class AlarmWorkerReceiver extends BroadcastReceiver {

    private final static String ACTION_START = "start";
    private final static String ACTION_CANCEL = "cancel";

    private final static Class<AlarmWorker> ALARM_WORKER_CLASS = AlarmWorker.class;
    private final static Constraints NETWORK_CONNECTED = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    private final static String ALARM_WORK_TAG = "AlarmWork";

    public static void startWork(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest
                .Builder(ALARM_WORKER_CLASS)
                .setConstraints(NETWORK_CONNECTED)
                .addTag(ALARM_WORK_TAG)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(ALARM_WORK_TAG, ExistingWorkPolicy.APPEND_OR_REPLACE, request);
    }

    public static void cancelWork(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(ALARM_WORK_TAG);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(ACTION_START)) {
            startWork(context);
        }

        if (action.equals(ACTION_CANCEL)) {
            cancelWork(context);
        }
    }
}

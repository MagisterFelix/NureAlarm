package com.nure.alarm.core.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.managers.FileManager;

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

        WorkManager.getInstance(context).enqueueUniqueWork(ALARM_WORK_TAG, ExistingWorkPolicy.APPEND_OR_REPLACE, request);
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(request.getId()).observeForever(workInfo -> {
            if (workInfo != null) {
                if (workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Alarm.enableAlarmWork(context, FileManager.readInfo(context));
                }
            }
        });
    }

    public static void cancelWork(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(ALARM_WORK_TAG);
    }
}

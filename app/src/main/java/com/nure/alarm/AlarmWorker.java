package com.nure.alarm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AlarmWorker extends Worker {

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Alarm.setAlarm(getApplicationContext());
        Data data = new Data.Builder().putString("result", "Alarm clock set").build();
        return Result.success(data);
    }
}
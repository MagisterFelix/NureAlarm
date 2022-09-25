package com.nure.alarm.core.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.api.Request;
import com.nure.alarm.core.managers.FileManager;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmWorker extends Worker {

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Calendar date = Calendar.getInstance();
        if (date.get(Calendar.HOUR_OF_DAY) > 7) {
            date.add(Calendar.DATE, 1);
        }

        try {
            Request request = new Request(getApplicationContext());
            request.getTimeTable(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date.getTime()),
                    FileManager.readInfo(getApplicationContext()).getGroup().getLong("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Alarm.enableAlarmWork(getApplicationContext(), FileManager.readInfo(getApplicationContext()));

        return Result.success();
    }
}

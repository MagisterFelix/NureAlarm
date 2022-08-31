package com.nure.alarm.core.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nure.alarm.core.FileManager;
import com.nure.alarm.core.api.Request;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmWorker extends Worker {

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private String getDate() {
        Calendar date = Calendar.getInstance();
        if (date.get(Calendar.HOUR_OF_DAY) > 7) {
            date.add(Calendar.DATE, 1);
        }
        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date.getTime());
    }

    private void makeRequest() {
        String date = getDate();
        try {
            Request request = new Request(getApplicationContext());
            request.getTimeTable(date, date, FileManager.readInfo(getApplicationContext()).getGroup().getLong("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        makeRequest();
        return Result.success();
    }
}

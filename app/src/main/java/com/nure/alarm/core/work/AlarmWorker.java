package com.nure.alarm.core.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nure.alarm.core.api.Request;
import com.nure.alarm.core.managers.FileManager;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmWorker extends Worker {

    private static final int SEVEN_HOURS = 7;
    private static final int ONE_DAY = 1;

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private String getDate() {
        Calendar date = Calendar.getInstance();
        if (date.get(Calendar.HOUR_OF_DAY) > SEVEN_HOURS) {
            date.add(Calendar.DATE, ONE_DAY);
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

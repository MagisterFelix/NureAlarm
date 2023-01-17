package com.nure.alarm.core.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.api.Request;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.DateRange;
import com.nure.alarm.core.models.Information;

import org.json.JSONException;

import java.util.Calendar;

public class AlarmWorker extends Worker {

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean setUpcomingLesson = getInputData().getBoolean(AlarmWorkerReceiver.UPCOMING_LESSON_KEY, false);

        Calendar dateTime = Calendar.getInstance();

        if (!setUpcomingLesson && dateTime.get(Calendar.HOUR_OF_DAY) > 6) {
            dateTime.add(Calendar.DATE, 1);
        }

        Calendar additionalDateTime= Calendar.getInstance();
        additionalDateTime.add(Calendar.DATE, 1);

        Information information = FileManager.readInfo(getApplicationContext());

        if (information.getAlarm().length() == 0) {
            try {
                Request request = new Request(getApplicationContext());

                if (setUpcomingLesson) {
                    request.getTimeTable(
                            new DateRange(dateTime, dateTime),
                            information.getGroup().getLong("id"),
                            new DateRange(additionalDateTime, additionalDateTime)
                    );
                } else {
                    request.getTimeTable(
                            new DateRange(dateTime, dateTime),
                            information.getGroup().getLong("id"),
                            null
                    );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Alarm.enableAlarmWork(getApplicationContext(), information);

        return Result.success();
    }
}

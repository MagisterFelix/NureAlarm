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
import com.nure.alarm.core.models.LessonsType;
import com.nure.alarm.core.models.Time;
import com.nure.alarm.core.utils.DateTimeUtils;
import com.nure.alarm.views.AlarmClockActivity;

import org.json.JSONException;

import java.util.Calendar;

public class AlarmWorker extends Worker {

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int lessonsType = getInputData().getInt(AlarmWorkerReceiver.LESSONS_TYPE_KEY, LessonsType.AUTO);
        boolean forceNotify = getInputData().getBoolean(AlarmWorkerReceiver.FORCE_NOTIFY_KEY, false);

        Calendar dateTime = Calendar.getInstance();
        if ((lessonsType == LessonsType.AUTO && dateTime.after(DateTimeUtils.getSpecificDateTime(new Time(7, 44)))) ||
                (lessonsType == LessonsType.TOMORROW_FIRST)) {
            dateTime.add(Calendar.DATE, 1);
        }

        Information information = FileManager.readInfo(getApplicationContext());

        if (information.getAlarm().length() == 0) {
            if (lessonsType == LessonsType.AUTO) {
                AlarmClockActivity.updateActivity(getApplicationContext(), true);
            }
            try {
                Request request = new Request(getApplicationContext());
                request.getTimeTable(new DateRange(dateTime), information.getGroup().getLong("id"), lessonsType, forceNotify);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (information.isEnabled()) {
            Alarm.enableAlarmWork(getApplicationContext(), information);
        }

        return Result.success();
    }
}

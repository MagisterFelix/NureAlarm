package com.nure.alarm.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentManager;

import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.utils.DateTimeUtils;
import com.nure.alarm.core.work.AlarmWorkManager;
import com.nure.alarm.views.AlarmClockActivity;
import com.nure.alarm.views.dialogs.DeletionConfirmationDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Alarm {

    public static void setAlarm(Context context, long time) {
        PendingIntent alarmClockActivity = PendingIntent.getActivity(context, 0,
                new Intent(context, AlarmClockActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent alarmReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time, alarmClockActivity), alarmReceiver);
    }

    public static void cancelAlarm(Context context) {
        PendingIntent alarmReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmReceiver);

        Information information = FileManager.readInfo(context);
        information.setAlarm(new JSONObject());
        FileManager.writeInfo(context, information);
    }

    public static void enableAlarmWork(Context context, Information information) {
        Calendar now = Calendar.getInstance();

        Calendar notificationTime = DateTimeUtils.getSpecificDateTime(information.getSettingTime());
        if (notificationTime.before(now) || notificationTime.equals(now)) {
            notificationTime.add(Calendar.DATE, 1);
        }

        AlarmWorkManager.setAlarmWork(context, notificationTime.getTimeInMillis());
    }

    public static void disableAlarmWork(Context context, FragmentManager fragmentManager) {
        AlarmWorkManager.cancelAlarmWork(context);

        if (FileManager.readInfo(context).getAlarm().length() != 0) {
            DeletionConfirmationDialog deletionConfirmationDialog = new DeletionConfirmationDialog();
            deletionConfirmationDialog.show(fragmentManager, DeletionConfirmationDialog.class.getSimpleName());
        }
    }

    public static void startAlarm(Context context, JSONObject lesson, Calendar alarmTime, Information information) {
        try {
            String formattedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(alarmTime.getTime());

            JSONObject alarm = new JSONObject();
            alarm.put("time", formattedTime);
            alarm.put("number", lesson.getInt("number"));
            alarm.put("name", lesson.getString("name"));

            information.setAlarm(alarm);
            FileManager.writeInfo(context, information);

            Alarm.setAlarm(context, alarmTime.getTimeInMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void stopAlarm(Context context) {
        Intent alarmService = new Intent(context, AlarmService.class);
        context.stopService(alarmService);

        cancelAlarm(context);
    }
}

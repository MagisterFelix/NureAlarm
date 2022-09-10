package com.nure.alarm.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.nure.alarm.R;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.notification.AlarmNotification;
import com.nure.alarm.core.work.AlarmWorkManager;
import com.nure.alarm.views.AlarmClockActivity;
import com.nure.alarm.views.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class Alarm {

    private final static int ONE_DAY = 1;
    private final static int ZERO_SECONDS = 0;
    private final static int MILLISECONDS_IN_MINUTE = 60000;
    private final static boolean HAVE_LESSONS = true;

    public static void setAlarm(Context context, long time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent mainActivity = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent alarmReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time, mainActivity), alarmReceiver);
        AlarmClockActivity.updateActivity(context);
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent alarmReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(alarmReceiver);

        Information information = FileManager.readInfo(context);
        information.setAlarm(new JSONObject());
        FileManager.writeInfo(context, information);
    }

    public static void disableAlarm(Context context) {
        Intent alarmService = new Intent(context, AlarmService.class);
        context.stopService(alarmService);
        cancelAlarm(context);
    }

    public static void enableAlarmWork(Context context, Information information) {
        Calendar now = Calendar.getInstance();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, information.getSettingHour());
        startTime.set(Calendar.MINUTE, information.getSettingMinute());
        startTime.set(Calendar.SECOND, ZERO_SECONDS);

        if (startTime.before(now)) {
            startTime.add(Calendar.DATE, ONE_DAY);
        }

        long delay = startTime.getTimeInMillis() - now.getTimeInMillis();
        AlarmWorkManager.startWork(context, delay);
    }

    public static void disableAlarmWork(Context context) {
        AlarmWorkManager.cancelWork(context);
        cancelAlarm(context);
    }

    public static void startAlarm(Context context, JSONObject lesson, int delay) {
        try {
            int[] time = Arrays.stream(lesson.getString("time").split("[: ]+")).mapToInt(Integer::parseInt).toArray();

            Calendar date = Calendar.getInstance();
            date.set(Calendar.HOUR_OF_DAY, time[0]);
            date.set(Calendar.MINUTE, time[1]);
            date.set(Calendar.SECOND, ZERO_SECONDS);
            date.add(Calendar.MILLISECOND, -(delay * MILLISECONDS_IN_MINUTE));

            String unformatted_message = context.getString(R.string.lessons_message);
            String message = String.format(Locale.getDefault(), unformatted_message, lesson.getString("number"), lesson.getString("name"));

            Information information = FileManager.readInfo(context);

            JSONObject alarm = new JSONObject();
            alarm.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date.getTime()));
            alarm.put("lesson", lesson.getString("name"));

            information.setAlarm(alarm);
            FileManager.writeInfo(context, information);

            Alarm.setAlarm(context, date.getTimeInMillis());
            AlarmNotification.sendNotification(context, message, HAVE_LESSONS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

package com.nure.alarm.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentManager;

import com.nure.alarm.R;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.managers.SessionManager;
import com.nure.alarm.core.models.DateRange;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.models.Time;
import com.nure.alarm.core.notification.AlarmNotification;
import com.nure.alarm.core.utils.DateTimeUtils;
import com.nure.alarm.core.work.AlarmWorkManager;
import com.nure.alarm.views.AlarmClockActivity;
import com.nure.alarm.views.MainActivity;
import com.nure.alarm.views.dialogs.DeletionConfirmationDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Alarm {

    public static void setAlarm(Context context, long time) {
        PendingIntent mainActivity = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent alarmReceiver = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time, mainActivity), alarmReceiver);

        AlarmClockActivity.updateActivity(context);
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

    public static void startAlarm(Context context, JSONObject lesson, Information information) {
        try {
            Calendar now = Calendar.getInstance();

            Calendar lessonTime = DateTimeUtils.getSpecificDateTime(new Time(lesson.getString("time")));
            if (!new DateRange(new SessionManager(context).fetchLessonsDate()).isToday()) {
                lessonTime.add(Calendar.DATE, 1);
                lessonTime.add(Calendar.MILLISECOND, -(information.getActivation() * DateTimeUtils.MILLISECONDS_IN_MINUTE));
            } else {
                if (now.before(DateTimeUtils.getSpecificDateTime(new Time(5, 45)))) {
                    lessonTime.add(Calendar.MILLISECOND, -(information.getActivation() * DateTimeUtils.MILLISECONDS_IN_MINUTE));
                }
            }

            String unformatted_message = context.getString(R.string.lessons_message);
            String message = String.format(
                    Locale.getDefault(),
                    unformatted_message, lesson.getInt("number"), lesson.getString("name")
            );
            String formatted_time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(lessonTime.getTime());

            JSONObject alarm = new JSONObject();
            alarm.put("time", formatted_time);
            alarm.put("number", lesson.getInt("number"));
            alarm.put("name", lesson.getString("name"));

            information.setAlarm(alarm);
            FileManager.writeInfo(context, information);

            Alarm.setAlarm(context, lessonTime.getTimeInMillis());
            AlarmNotification.sendNotification(context, message, true);
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

package com.nure.alarm.core;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.provider.AlarmClock;
import android.widget.Toast;

import com.nure.alarm.R;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.notification.AlarmNotification;
import com.nure.alarm.core.work.AlarmWorkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class Alarm {
    private final static int ZERO_SECONDS = 0;
    private final static int ONE_DAY = 1;
    private final static boolean SKIP_UI = true;

    public static void setAlarm(Context context, int hour, int minute) {
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, "NureAlarm");
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, SKIP_UI);
        context.startActivity(alarmIntent);
    }

    public static void cancelAlarm(Context context) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(new SessionManager(context).fetchLocale()));
        Toast.makeText(context, context.createConfigurationContext(configuration).getString(R.string.remove_alarm_message), Toast.LENGTH_LONG).show();

        Intent alarmIntent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
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

    public static void disableAlarmWork(Context context, Information information) {
        AlarmWorkManager.cancelWork(context);
        if (information.isSet()) {
            information.setSet(false);
            FileManager.writeInfo(context, information);
            cancelAlarm(context);
        }
    }

    public static void startAlarm(Context context, JSONObject lesson, int delay) {
        try {
            String string_time = lesson.getString("time").split(" ")[0];
            Calendar time = Calendar.getInstance();
            time.setTime(Objects.requireNonNull(new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(string_time)));
            time.add(Calendar.MILLISECOND, -(delay * 60000));

            Alarm.setAlarm(context, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));

            String unformatted_message = "Alarm clock set for %s lesson - %s";
            String message = String.format(Locale.getDefault(), unformatted_message, lesson.getString("number"), lesson.getString("name"));
            AlarmNotification.sendNotification(context, message, true);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }
}

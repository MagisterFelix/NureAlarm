package com.nure.alarm.core.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.nure.alarm.R;

import java.util.Calendar;

public class SessionManager {

    private final SharedPreferences sharedPreferences;

    private static final String KEY_GROUP_REQUEST_TIME = "group_request_time";
    private static final String KEY_SUBJECTS_REQUEST_TIME = "subjects_request_time";
    private static final String KEY_LESSONS_DATE_TIME = "lessons_date_time";
    private static final String KEY_LOCALE = "locale";
    private static final String KEY_LAST_ACTIVITY = "last_activity";

    private static final String DEFAULT_LOCALE = "en";
    private static final String DEFAULT_ACTIVITY = "MainActivity";

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    public void saveGroupRequestTime(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_GROUP_REQUEST_TIME, time);
        editor.apply();
    }

    public long fetchGroupRequestTime() {
        Calendar fromStart = Calendar.getInstance();
        fromStart.setTimeInMillis(0);
        return sharedPreferences.getLong(KEY_GROUP_REQUEST_TIME, fromStart.getTimeInMillis());
    }

    public void saveSubjectsRequestTime(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_SUBJECTS_REQUEST_TIME, time);
        editor.apply();
    }

    public long fetchSubjectsRequestTime() {
        Calendar fromStart = Calendar.getInstance();
        fromStart.setTimeInMillis(0);
        return sharedPreferences.getLong(KEY_SUBJECTS_REQUEST_TIME, fromStart.getTimeInMillis());
    }

    public void saveLessonsDateTime(Calendar dateTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LESSONS_DATE_TIME, dateTime.getTimeInMillis());
        editor.apply();
    }

    public Calendar fetchLessonsDateTime() {
        Calendar dateTime = Calendar.getInstance();
        dateTime.setTimeInMillis(0);
        dateTime.setTimeInMillis(sharedPreferences.getLong(KEY_LESSONS_DATE_TIME, dateTime.getTimeInMillis()));
        return dateTime;
    }

    public void saveLocale(String locale) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOCALE, locale);
        editor.apply();
    }

    public String fetchLocale() {
        return sharedPreferences.getString(KEY_LOCALE, DEFAULT_LOCALE);
    }

    public void saveLastActivity(String locale) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LAST_ACTIVITY, locale);
        editor.apply();
    }

    public String fetchLastActivity() {
        return sharedPreferences.getString(KEY_LAST_ACTIVITY, DEFAULT_ACTIVITY);
    }
}

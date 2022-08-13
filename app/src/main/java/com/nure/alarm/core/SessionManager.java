package com.nure.alarm.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.nure.alarm.R;

import java.util.Calendar;

public class SessionManager {
    private final SharedPreferences sharedPreferences;

    private static final String KEY_TIME = "time";

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    public void saveTime(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_TIME, time);
        editor.apply();
    }

    public long fetchTime() {
        Calendar fromStart = Calendar.getInstance();
        fromStart.setTimeInMillis(0);
        return sharedPreferences.getLong(KEY_TIME, fromStart.getTimeInMillis());
    }
}

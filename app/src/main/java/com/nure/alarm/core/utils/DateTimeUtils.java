package com.nure.alarm.core.utils;

import com.nure.alarm.core.models.Time;

import java.util.Calendar;

public class DateTimeUtils {

    public static final int MILLISECONDS_IN_MINUTE = 60000;

    public static Calendar getSpecificDateTime(Time time) {
        Calendar dateTime = Calendar.getInstance();

        dateTime.set(Calendar.HOUR_OF_DAY, time.getHour());
        dateTime.set(Calendar.MINUTE, time.getMinute());
        dateTime.set(Calendar.SECOND, 0);

        return dateTime;
    }
}

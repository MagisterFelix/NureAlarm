package com.nure.alarm.core.utils;

import android.content.Context;

import com.nure.alarm.core.managers.SessionManager;
import com.nure.alarm.core.models.DateRange;
import com.nure.alarm.core.models.Information;
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

    public static Calendar getAlarmTime(Context context, Calendar lessonTime, Information information) {
        Calendar now = Calendar.getInstance();

        if (!new DateRange(new SessionManager(context).fetchLessonsDateTime()).isToday()) {
            lessonTime.add(Calendar.DATE, 1);
            lessonTime.add(Calendar.MILLISECOND, -(information.getActivation() * DateTimeUtils.MILLISECONDS_IN_MINUTE));
        } else {
            if (now.before(DateTimeUtils.getSpecificDateTime(new Time(5, 45)))) {
                lessonTime.add(Calendar.MILLISECOND, -(information.getActivation() * DateTimeUtils.MILLISECONDS_IN_MINUTE));
            }
        }

        return lessonTime;
    }
}

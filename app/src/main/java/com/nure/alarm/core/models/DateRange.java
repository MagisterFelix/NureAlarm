package com.nure.alarm.core.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateRange {

    private final Calendar from_date;
    private final Calendar to_date;

    public DateRange(Calendar from_date, Calendar to_date) {
        this.from_date = from_date;
        this.to_date = to_date;
    }

    public String getRange() {
        String from_date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this.from_date.getTime());
        String to_date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this.to_date.getTime());

        return from_date + "," + to_date;
    }
}

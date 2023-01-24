package com.nure.alarm.core.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateRange {

    private final Calendar fromDate;
    private final Calendar toDate;

    public DateRange(Calendar fromDate, Calendar toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public DateRange(Calendar date) {
        this.fromDate = date;
        this.toDate = date;
    }

    public boolean isToday() {
        Calendar now = Calendar.getInstance();

        return now.get(Calendar.DATE) == fromDate.get(Calendar.DATE) &&
                now.get(Calendar.MONTH) == fromDate.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == fromDate.get(Calendar.YEAR);
    }

    public Calendar getFromDate() {
        return fromDate;
    }

    public String getRange() {
        String from_date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this.fromDate.getTime());
        String to_date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this.toDate.getTime());

        return from_date + "," + to_date;
    }
}

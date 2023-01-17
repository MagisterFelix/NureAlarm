package com.nure.alarm.core.models;

import java.util.Arrays;

public class Time {

    private final int hour;
    private final int minute;

    public Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public Time(String time) {
        int[] hm = Arrays.stream(time.split("[: ]+")).mapToInt(Integer::parseInt).toArray();
        this.hour = hm[0];
        this.minute = hm[1];
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }
}

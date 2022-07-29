package com.nure.alarm;

public class Information {
    private boolean status;
    private int alarmHour;
    private int alarmMinute;

    public Information(boolean status, int alarmHour, int alarmMinute) {
        this.status = status;
        this.alarmHour = alarmHour;
        this.alarmMinute = alarmMinute;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getAlarmHour() {
        return alarmHour;
    }

    public void setAlarmHour(int alarmHour) {
        this.alarmHour = alarmHour;
    }

    public int getAlarmMinute() {
        return alarmMinute;
    }

    public void setAlarmMinute(int alarmMinute) {
        this.alarmMinute = alarmMinute;
    }
}

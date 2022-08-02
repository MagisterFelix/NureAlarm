package com.nure.alarm.core;

public class Information {
    private boolean status;
    private int alarmSettingHour;
    private int alarmSettingMinute;

    public Information(boolean status, int alarmSettingHour, int alarmSettingMinute) {
        this.status = status;
        this.alarmSettingHour = alarmSettingHour;
        this.alarmSettingMinute = alarmSettingMinute;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getAlarmSettingHour() {
        return alarmSettingHour;
    }

    public void setAlarmSettingHour(int alarmSettingHour) {
        this.alarmSettingHour = alarmSettingHour;
    }

    public int getAlarmSettingMinute() {
        return alarmSettingMinute;
    }

    public void setAlarmSettingMinute(int alarmSettingMinute) {
        this.alarmSettingMinute = alarmSettingMinute;
    }
}

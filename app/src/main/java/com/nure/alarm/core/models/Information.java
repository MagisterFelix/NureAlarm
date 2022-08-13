package com.nure.alarm.core.models;

public class Information {
    private boolean status;
    private int settingHour;
    private int settingMinute;
    private String group;

    public Information(boolean status, int settingHour, int settingMinute, String group) {
        this.status = status;
        this.settingHour = settingHour;
        this.settingMinute = settingMinute;
        this.group = group;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getSettingHour() {
        return settingHour;
    }

    public void setSettingHour(int settingHour) {
        this.settingHour = settingHour;
    }

    public int getSettingMinute() {
        return settingMinute;
    }

    public void setSettingMinute(int settingMinute) {
        this.settingMinute = settingMinute;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}

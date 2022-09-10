package com.nure.alarm.core.models;

import org.json.JSONArray;
import org.json.JSONObject;

public class Information {

    private boolean enabled;
    private int settingHour;
    private int settingMinute;
    private int delay;
    private JSONObject group;
    private JSONArray lessons;
    private JSONObject alarm;

    public Information(boolean enabled, int settingHour, int settingMinute, int delay, JSONObject group, JSONArray lessons, JSONObject alarm) {
        this.enabled = enabled;
        this.settingHour = settingHour;
        this.settingMinute = settingMinute;
        this.delay = delay;
        this.group = group;
        this.lessons = lessons;
        this.alarm = alarm;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public JSONObject getGroup() {
        return group;
    }

    public void setGroup(JSONObject group) {
        this.group = group;
    }

    public JSONArray getLessons() {
        return lessons;
    }

    public void setLessons(JSONArray lessons) {
        this.lessons = lessons;
    }

    public JSONObject getAlarm() {
        return alarm;
    }

    public void setAlarm(JSONObject alarm) {
        this.alarm = alarm;
    }
}

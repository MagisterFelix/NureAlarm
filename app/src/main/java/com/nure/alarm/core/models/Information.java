package com.nure.alarm.core.models;

import org.json.JSONArray;
import org.json.JSONObject;

public class Information {

    private boolean enabled;
    private Time settingTime;
    private int activation;
    private JSONObject group;
    private JSONArray excludedSubjects;
    private JSONArray lessons;
    private JSONObject alarm;

    public Information(boolean enabled, Time settingTime, int activation, JSONObject group, JSONArray excludedSubjects, JSONArray lessons, JSONObject alarm) {
        this.enabled = enabled;
        this.settingTime = settingTime;
        this.activation = activation;
        this.group = group;
        this.excludedSubjects = excludedSubjects;
        this.lessons = lessons;
        this.alarm = alarm;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Time getSettingTime() {
        return settingTime;
    }

    public void setSettingTime(Time settingTime) {
        this.settingTime = settingTime;
    }

    public int getActivation() {
        return activation;
    }

    public void setActivation(int activation) {
        this.activation = activation;
    }

    public JSONObject getGroup() {
        return group;
    }

    public void setGroup(JSONObject group) {
        this.group = group;
    }

    public JSONArray getExcludedSubjects() {
        return excludedSubjects;
    }

    public void setExcludedSubjects(JSONArray excludedSubjects) {
        this.excludedSubjects = excludedSubjects;
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

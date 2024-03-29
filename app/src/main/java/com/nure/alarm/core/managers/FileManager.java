package com.nure.alarm.core.managers;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.models.Time;
import com.nure.alarm.core.utils.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {

    private static final String INFO_FILE = "info.json";
    private static final String GROUPS_FILE = "groups.json";
    private static final String SUBJECTS_FILE = "subjects.json";

    private static final boolean DISABLED = false;
    private static final Time DEFAULT_SETTING_TIME = new Time(20, 0);
    private static final int DEFAULT_ACTIVATION = 30;
    private static final JSONObject UNDEFINED_GROUP = new JSONObject();
    private static final JSONArray UNDEFINED_EXCLUDED_SUBJECTS = new JSONArray();
    private static final JSONArray UNDEFINED_LESSONS = new JSONArray();
    private static final JSONObject UNDEFINED_ALARM = new JSONObject();

    private static void createIfNotExist(Context context, String file) {
        try {
            context.openFileInput(file);
        } catch (FileNotFoundException e) {
            FileOutputStream fos;
            try {
                fos = context.openFileOutput(file, Context.MODE_PRIVATE);
                fos.write("{}".getBytes());
                fos.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private static String readJSON(Context context, String file) {
        createIfNotExist(context, file);

        StringBuilder data = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.openFileInput(file)));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                data.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.toString();
    }

    private static void writeJSON(Context context, JSONObject object, String file) {
        try {
            FileOutputStream fos = context.openFileOutput(file, Context.MODE_PRIVATE);
            fos.write(object.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Information readInfo(Context context) {
        try {
            JSONObject object = new JSONObject(readJSON(context, INFO_FILE));

            return new Information(
                    object.getBoolean("enabled"),
                    new Time(object.getInt("settingHour"), object.getInt("settingMinute")),
                    object.getInt("activation"),
                    object.getJSONObject("group"),
                    object.getJSONArray("excludedSubjects"),
                    object.getJSONArray("lessons"),
                    object.getJSONObject("alarm")
            );
        } catch (JSONException e) {
            Information information = new Information(
                    DISABLED,
                    DEFAULT_SETTING_TIME,
                    DEFAULT_ACTIVATION,
                    UNDEFINED_GROUP,
                    UNDEFINED_EXCLUDED_SUBJECTS,
                    UNDEFINED_LESSONS,
                    UNDEFINED_ALARM
            );

            writeInfo(context, information);

            return information;
        }
    }

    public static void writeInfo(Context context, Information information) {
        try {
            JSONObject object = new JSONObject();

            object.put("enabled", information.isEnabled());
            object.put("settingHour", information.getSettingTime().getHour());
            object.put("settingMinute", information.getSettingTime().getMinute());
            object.put("group", information.getGroup());
            object.put("excludedSubjects", information.getExcludedSubjects());
            object.put("lessons", information.getLessons());
            object.put("activation", information.getActivation());
            object.put("alarm", information.getAlarm());

            writeJSON(context, object, INFO_FILE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Integer> readGroups(Context context) {
        HashMap<String, Integer> groups = new HashMap<>();

        try {
            for (JsonNode node : new ObjectMapper().readTree(readJSON(context, GROUPS_FILE)).findValues("groups")) {
                for (Object object : node) {
                    JSONObject jsonObject = new JSONObject(object.toString());
                    groups.put(jsonObject.getString("name"), jsonObject.getInt("id"));
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return groups;
    }

    public static void writeGroups(Context context, String groups) {
        try {
            JSONObject object = new JSONObject(groups);
            writeJSON(context, object, GROUPS_FILE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> readSubjects(Context context) {
        ArrayList<String> subjects = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONObject(readJSON(context, SUBJECTS_FILE)).getJSONArray("subjects");
            subjects.addAll(JSONUtils.getArrayListFromJSONArray(jsonArray));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    public static void writeSubjects(Context context, JSONArray subjects) {
        try {
            JSONObject object = new JSONObject();
            object.put("subjects", subjects);
            writeJSON(context, object, SUBJECTS_FILE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

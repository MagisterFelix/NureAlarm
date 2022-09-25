package com.nure.alarm.core.managers;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nure.alarm.core.models.Information;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class FileManager {

    private static final String INFO_FILE = "info.json";
    private static final String GROUPS_FILE = "groups.json";

    private static final boolean DISABLED = false;
    private static final int DEFAULT_SETTING_HOUR = 20;
    private static final int DEFAULT_SETTING_MINUTE = 0;
    private static final int DEFAULT_DELAY = 30;
    private static final JSONObject UNDEFINED_GROUP = new JSONObject();
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
            JSONObject object =  new JSONObject(readJSON(context, INFO_FILE));

            return new Information(
                    object.getBoolean("enabled"),
                    object.getInt("settingHour"),
                    object.getInt("settingMinute"),
                    object.getInt("delay"),
                    object.getJSONObject("group"),
                    object.getJSONArray("lessons"),
                    object.getJSONObject("alarm")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Information(DISABLED, DEFAULT_SETTING_HOUR, DEFAULT_SETTING_MINUTE, DEFAULT_DELAY, UNDEFINED_GROUP, UNDEFINED_LESSONS, UNDEFINED_ALARM);
    }

    public static void writeInfo(Context context, Information information) {
        try {
            JSONObject object = new JSONObject();

            object.put("enabled", information.isEnabled());
            object.put("settingHour", information.getSettingHour());
            object.put("settingMinute", information.getSettingMinute());
            object.put("group", information.getGroup());
            object.put("lessons", information.getLessons());
            object.put("delay", information.getDelay());
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

            return groups;
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
}

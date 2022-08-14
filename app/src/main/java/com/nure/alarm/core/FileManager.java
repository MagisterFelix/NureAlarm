package com.nure.alarm.core;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nure.alarm.core.models.Information;

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

    private static final boolean STATUS_OFF = false;
    private static final int UNDEFINED_SETTING_HOUR = -1;
    private static final int UNDEFINED_SETTING_MINUTE = -1;
    private static final JSONObject UNDEFINED_GROUP = new JSONObject();

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
                    object.getBoolean("status"),
                    object.getInt("settingHour"),
                    object.getInt("settingMinute"),
                    object.getJSONObject("group")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Information(STATUS_OFF, UNDEFINED_SETTING_HOUR, UNDEFINED_SETTING_MINUTE, UNDEFINED_GROUP);
    }

    public static void writeInfo(Context context, Information information) {
        try {
            JSONObject object = new JSONObject();
            object.put("status", information.getStatus());
            object.put("settingHour", information.getSettingHour());
            object.put("settingMinute", information.getSettingMinute());
            object.put("group", information.getGroup());
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

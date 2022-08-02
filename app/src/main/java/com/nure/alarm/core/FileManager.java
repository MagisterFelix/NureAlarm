package com.nure.alarm.core;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileManager {
    public static final String FILE_NAME = "alarm_info.json";

    private static void createIfNotExist(Context context) {
        try {
            context.openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            FileOutputStream fos;
            try {
                fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                fos.write(("{\"status\": false, \"alarmSettingHour\": 0, \"alarmSettingMinute\": 0}").getBytes());
                fos.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private static String readJSON(Context context) {
        createIfNotExist(context);

        StringBuilder data = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.openFileInput(FILE_NAME)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                data.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.toString();
    }

    private static void writeJSON(Context context, JSONObject object) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(object.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Information getInfo(Context context) {
        try {
            JSONObject object =  new JSONObject(readJSON(context));

            return new Information(
                    object.getBoolean("status"),
                    object.getInt("alarmSettingHour"),
                    object.getInt("alarmSettingMinute")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void updateInfo(Context context, Information information) {
        JSONObject object = new JSONObject();

        try {
            object.put("status", information.getStatus());
            object.put("alarmSettingHour", information.getAlarmSettingHour());
            object.put("alarmSettingMinute", information.getAlarmSettingMinute());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeJSON(context, object);
    }
}

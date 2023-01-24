package com.nure.alarm.core.utils;

import android.content.Context;

import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.models.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class JSONUtils {

    public static ArrayList<String> getArrayListFromJSONArray(JSONArray jsonArray) {
        ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); ++i) {
            try {
                arrayList.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return arrayList;
    }

    public static void filterLessonsByCurrentTime(Context context) {
        Information information = FileManager.readInfo(context);
        JSONArray lessons = information.getLessons();

        Calendar now = Calendar.getInstance();

        try {
            for (int i = 0; i < lessons.length(); ++i) {
                JSONObject lesson = lessons.getJSONObject(i);
                Calendar lessonTime = DateTimeUtils.getSpecificDateTime(new Time(lesson.getString("time")));

                if (lessonTime.after(now)) {
                    break;
                }

                lessons.remove(i);
                --i;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        information.setLessons(lessons);
        FileManager.writeInfo(context, information);
    }
}

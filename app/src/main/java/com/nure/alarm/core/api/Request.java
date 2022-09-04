package com.nure.alarm.core.api;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.FileManager;
import com.nure.alarm.core.SessionManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.notification.AlarmNotification;
import com.nure.alarm.views.dialogs.FailedGroupsRequestDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Request {
    private final Context context;
    private final ApiClient apiClient;
    private final SessionManager sessionManager;

    public Request(Context context) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(new SessionManager(context).fetchLocale()));

        this.context = context.createConfigurationContext(configuration);
        this.apiClient = new ApiClient();
        this.sessionManager = new SessionManager(context);
    }

    public void getGroups(FragmentManager fragmentManager) {
        Calendar now = Calendar.getInstance();
        Calendar lastTime = Calendar.getInstance();
        lastTime.setTimeInMillis(sessionManager.fetchTime());
        if (TimeUnit.MILLISECONDS.toDays(now.getTimeInMillis() - lastTime.getTimeInMillis()) > 0) {
            apiClient.getApiService().group().enqueue(new Callback<Object>() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    Gson gson = new GsonBuilder().
                            registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
                                if (src == src.longValue()) {
                                    return new JsonPrimitive(src.longValue());
                                }
                                return new JsonPrimitive(src);
                            }).create();

                    FileManager.writeGroups(context, gson.toJson(response.body()));
                    sessionManager.saveTime(Calendar.getInstance().getTimeInMillis());
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull Throwable throwable) {
                    FailedGroupsRequestDialog failedGroupsRequestDialog = new FailedGroupsRequestDialog();
                    failedGroupsRequestDialog.show(fragmentManager, "FailedGroupsRequestDialog");
                }
            });
        }
    }

    public void getTimeTable(String from_date, String to_date, long group_id) {
        String unformatted_query = "778:201::::201:P201_FIRST_DATE,P201_LAST_DATE,P201_GROUP,P201_POTOK:%s,%s,%d,0:";
        String query = String.format(Locale.getDefault(), unformatted_query, from_date, to_date, group_id);
        apiClient.getApiService().timetable(query).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONArray lessons = new JSONArray();

                    String html = Objects.requireNonNull(response.body()).string();
                    Document document = Jsoup.parse(html);
                    Elements elements = document.select("tr");
                    for (Element element : elements.subList(2, elements.size() - 1)) {
                        if (element.children().size() == 3){
                            JSONObject object = new JSONObject();
                            object.put("number", element.child(0).text());
                            object.put("time", element.child(1).text());
                            object.put("name", element.child(2).text());
                            lessons.put(object);
                        }
                    }

                    Information information = FileManager.readInfo(context);
                    information.setLessons(lessons);
                    information.setSet(lessons.length() != 0);
                    FileManager.writeInfo(context, information);

                    if (lessons.length() == 0) {
                        AlarmNotification.sendNotification(context, context.getString(R.string.no_lessons), false);
                    } else {
                        Alarm.startAlarm(context, lessons.getJSONObject(0), information.getDelay());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Information information = FileManager.readInfo(context);
                information.setLessons(new JSONArray().put(context.getString(R.string.failed_timetable_request_message)));
                FileManager.writeInfo(context, information);

                AlarmNotification.sendNotification(context, context.getString(R.string.failed_timetable_request_message), false);
            }
        });
    }
}

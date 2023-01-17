package com.nure.alarm.core.api;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.managers.ContextManager;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.managers.SessionManager;
import com.nure.alarm.core.models.DateRange;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.notification.AlarmNotification;
import com.nure.alarm.views.AlarmClockActivity;
import com.nure.alarm.views.MainActivity;
import com.nure.alarm.views.dialogs.FailedGroupsRequestDialog;
import com.nure.alarm.views.dialogs.ReceivingGroupsDialog;

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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Request {

    private final Context context;
    private final ApiClient apiClient;

    public Request(Context context) {
        this.context = ContextManager.getLocaleContext(context);
        this.apiClient = new ApiClient();
    }

    public void getGroups(Activity activity, FragmentManager fragmentManager, TextView groupTextView) {
        ReceivingGroupsDialog receivingGroupsDialog = new ReceivingGroupsDialog();
        receivingGroupsDialog.setCancelable(false);
        receivingGroupsDialog.show(fragmentManager, ReceivingGroupsDialog.class.getSimpleName());

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
                new SessionManager(context).saveGroupRequestTime(Calendar.getInstance().getTimeInMillis());

                try {
                    receivingGroupsDialog.dismiss();
                    MainActivity.showGroups(activity, context, groupTextView);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable throwable) {
                try {
                    receivingGroupsDialog.dismiss();
                    FailedGroupsRequestDialog failedGroupsRequestDialog = new FailedGroupsRequestDialog();
                    failedGroupsRequestDialog.show(fragmentManager, FailedGroupsRequestDialog.class.getSimpleName());
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getTimeTable(DateRange dateRange, long group_id) {
        String unformatted_query = "778:201::::201:P201_FIRST_DATE,P201_LAST_DATE,P201_GROUP,P201_POTOK:%s,%d,0:";
        String query = String.format(Locale.getDefault(), unformatted_query, dateRange.getRange(), group_id);

        apiClient.getApiService().timetable(query).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONArray lessons = new JSONArray();

                    String html = Objects.requireNonNull(response.body()).string();
                    Document document = Jsoup.parse(html);
                    Element table = document.select("table[class=MainTT]").first();
                    Elements elements = table.select("tr:has(td[class=left])");

                    for (Element element : elements) {
                        JSONObject lesson = new JSONObject();

                        lesson.put("number", Integer.parseInt(element.child(0).text()));
                        lesson.put("time", element.child(1).text());
                        lesson.put("name", element.child(2).select("a").text());

                        lessons.put(lesson);
                    }

                    Information information = FileManager.readInfo(context);
                    information.setLessons(lessons);
                    FileManager.writeInfo(context, information);

                    if (lessons.length() == 0) {
                        AlarmNotification.sendNotification(context, context.getString(R.string.no_lessons), false);
                        AlarmClockActivity.updateActivity(context);
                    } else {
                        Alarm.startAlarm(context, lessons.getJSONObject(0), information);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                AlarmNotification.sendNotification(context, context.getString(R.string.failed_timetable_request_message), null);
                AlarmClockActivity.updateActivity(context);
            }
        });
    }
}

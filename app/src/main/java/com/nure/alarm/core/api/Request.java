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
import com.nure.alarm.core.models.Time;
import com.nure.alarm.core.notification.AlarmNotification;
import com.nure.alarm.core.utils.DateTimeUtils;
import com.nure.alarm.core.utils.JSONUtils;
import com.nure.alarm.views.AlarmClockActivity;
import com.nure.alarm.views.MainActivity;
import com.nure.alarm.views.dialogs.EmptyListOfElementsDialog;
import com.nure.alarm.views.dialogs.FailedGroupsRequestDialog;
import com.nure.alarm.views.dialogs.FailedSubjectsRequestDialog;
import com.nure.alarm.views.dialogs.ReceivingGroupsDialog;
import com.nure.alarm.views.dialogs.ReceivingSubjectsDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    if (FileManager.readGroups(context).size() != 0) {
                        MainActivity.showGroups(activity, context, groupTextView);
                    } else {
                        EmptyListOfElementsDialog emptyListOfElementsDialog = new EmptyListOfElementsDialog();
                        emptyListOfElementsDialog.show(fragmentManager, EmptyListOfElementsDialog.class.getSimpleName());
                    }
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

    public void getSubjects(Activity activity, FragmentManager fragmentManager, TextView excludedSubjectsTextView, DateRange dateRange, long group_id) {
        ReceivingSubjectsDialog receivingSubjectsDialog = new ReceivingSubjectsDialog();
        receivingSubjectsDialog.setCancelable(false);
        receivingSubjectsDialog.show(fragmentManager, ReceivingSubjectsDialog.class.getSimpleName());

        String unformattedQuery = "778:201::::201:P201_FIRST_DATE,P201_LAST_DATE,P201_GROUP,P201_POTOK:%s,%d,0:";
        String query = String.format(Locale.getDefault(), unformattedQuery, dateRange.getRange(), group_id);

        apiClient.getApiService().timetable(query).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONArray subjects = new JSONArray();

                    String html = Objects.requireNonNull(response.body()).string();
                    Document document = Jsoup.parse(html);
                    Element table = document.select("table[class=footer]").first();
                    if (table != null) {
                        Elements elements = table.select("td[class=name]");
                        for (Element element : elements) {
                            subjects.put(element.text());
                        }

                        FileManager.writeSubjects(context, subjects);

                        Information information = FileManager.readInfo(context);

                        ArrayList<String> subjectsArray = JSONUtils.getArrayListFromJSONArray(subjects);
                        ArrayList<String> excludedSubjectsArray = JSONUtils.getArrayListFromJSONArray(information.getExcludedSubjects());

                        if (excludedSubjectsArray.size() != 0) {
                            int i = 0;
                            while (i < subjectsArray.size()) {
                                if (excludedSubjectsArray.contains(subjectsArray.get(i))) {
                                    break;
                                }
                                ++i;
                            }
                            if (i == subjectsArray.size()) {
                                information.setExcludedSubjects(new JSONArray());
                                FileManager.writeInfo(context, information);
                                excludedSubjectsTextView.setText("");
                            }
                        }

                        try {
                            receivingSubjectsDialog.dismiss();
                            if (FileManager.readSubjects(context).size() != 0) {
                                MainActivity.showSubjects(activity, context, excludedSubjectsTextView);
                            } else {
                                EmptyListOfElementsDialog emptyListOfElementsDialog = new EmptyListOfElementsDialog();
                                emptyListOfElementsDialog.show(fragmentManager, EmptyListOfElementsDialog.class.getSimpleName());
                            }
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    } else {
                        receivingSubjectsDialog.dismiss();
                        EmptyListOfElementsDialog emptyListOfElementsDialog = new EmptyListOfElementsDialog();
                        emptyListOfElementsDialog.show(fragmentManager, EmptyListOfElementsDialog.class.getSimpleName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable throwable) {
                try {
                    receivingSubjectsDialog.dismiss();
                    FailedSubjectsRequestDialog failedSubjectsRequestDialog = new FailedSubjectsRequestDialog();
                    failedSubjectsRequestDialog.show(fragmentManager, FailedSubjectsRequestDialog.class.getSimpleName());
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getTimeTable(DateRange dateRange, long group_id, int lessonsType) {
        String unformattedQuery = "778:201::::201:P201_FIRST_DATE,P201_LAST_DATE,P201_GROUP,P201_POTOK:%s,%d,0:";
        String query = String.format(Locale.getDefault(), unformattedQuery, dateRange.getRange(), group_id);

        apiClient.getApiService().timetable(query).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONArray lessons = new JSONArray();

                    String html = Objects.requireNonNull(response.body()).string();
                    Document document = Jsoup.parse(html);
                    Element table = document.select("table[class=MainTT]").first();
                    Elements elements = table.select("tr:has(td[class=left])");

                    Information information = FileManager.readInfo(context);
                    ArrayList<String> excludedSubjects = JSONUtils.getArrayListFromJSONArray(information.getExcludedSubjects());

                    for (Element element : elements) {
                        ArrayList<String> parsedLessons = new ArrayList<>();

                        Pattern pattern = Pattern.compile("(\\S+\\s*\\s*\\S+)");
                        Matcher matcher = pattern.matcher(element.child(2).select("a").text());
                        while (matcher.find()) {
                            parsedLessons.add(matcher.group());
                        }

                        StringBuilder filteredLessons = new StringBuilder();
                        for (String parsedLesson : parsedLessons) {
                            if (!excludedSubjects.contains(parsedLesson.substring(0, parsedLesson.indexOf(" ")))) {
                                filteredLessons.append(parsedLesson).append(" ");
                            }
                        }

                        if (filteredLessons.length() != 0) {
                            JSONObject lesson = new JSONObject();

                            lesson.put("number", Integer.parseInt(element.child(0).text()));
                            lesson.put("time", element.child(1).text());
                            lesson.put("name", filteredLessons.toString().trim());

                            lessons.put(lesson);
                        }
                    }

                    information.setLessons(lessons);
                    FileManager.writeInfo(context, information);

                    new SessionManager(context).saveLessonsDateTime(dateRange.getFromDate());

                    if (dateRange.isToday()) {
                        JSONUtils.filterLessonsByCurrentTime(context);
                    }

                    lessons = FileManager.readInfo(context).getLessons();

                    if (lessons.length() != 0) {
                        JSONObject nextLesson = lessons.getJSONObject(0);

                        Alarm.startAlarm(
                                context,
                                nextLesson,
                                DateTimeUtils.getAlarmTime(
                                        context,
                                        DateTimeUtils.getSpecificDateTime(new Time(nextLesson.getString("time"))),
                                        information
                                ),
                                information
                        );

                        String unformattedMessage = context.getString(R.string.lessons_message);
                        String message = String.format(
                                Locale.getDefault(),
                                unformattedMessage, nextLesson.getInt("number"), nextLesson.getString("name")
                        );

                        AlarmNotification.sendNotification(context, message, true, null);
                        AlarmClockActivity.updateActivity(context, false);
                    } else {
                        AlarmNotification.sendNotification(context, context.getString(R.string.no_lessons), false, null);
                        AlarmClockActivity.updateActivity(context, false);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                AlarmNotification.sendNotification(context, context.getString(R.string.failed_timetable_request_message), null, lessonsType);
                AlarmClockActivity.updateActivity(context, false);
            }
        });
    }
}

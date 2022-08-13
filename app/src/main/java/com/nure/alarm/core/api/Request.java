package com.nure.alarm.core.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.nure.alarm.core.FileManager;
import com.nure.alarm.core.SessionManager;
import com.nure.alarm.views.dialogs.FailedGroupsRequestDialog;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Request {
    private final Context context;
    private final ApiClient apiClient;
    private final SessionManager sessionManager;

    public Request(Context context) {
        this.context = context;
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
}

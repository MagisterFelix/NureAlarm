package com.nure.alarm.core.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET(Endpoint.GROUP)
    Call<Object> group();

    @GET(Endpoint.TIME_TABLE)
    Call<ResponseBody> timetable(@Query(value="p", encoded=true) String query);
}

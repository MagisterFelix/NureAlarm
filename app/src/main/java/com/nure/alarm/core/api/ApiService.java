package com.nure.alarm.core.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET(Endpoint.GROUP)
    Call<Object> group();
}

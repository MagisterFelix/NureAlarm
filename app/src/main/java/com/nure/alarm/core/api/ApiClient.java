package com.nure.alarm.core.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private final static Class<ApiService> API_SERVICE_CLASS = ApiService.class;

    private ApiService apiService;

    public ApiService getApiService() {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit
                    .Builder()
                    .baseUrl(Endpoint.BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(API_SERVICE_CLASS);
        }
        return apiService;
    }
}

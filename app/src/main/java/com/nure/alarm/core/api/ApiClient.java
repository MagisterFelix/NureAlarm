package com.nure.alarm.core.api;

import org.riversun.okhttp3.OkHttp3CookieHelper;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private ApiService apiService;
    private static final Class<ApiService> API_SERVICE_CLASS = ApiService.class;

    public ApiService getApiService() {
        if (apiService == null) {
            OkHttp3CookieHelper cookieHelper = new OkHttp3CookieHelper();
            cookieHelper.setCookie(Endpoint.BASE, "cookieName", "cookieValue");

            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(cookieHelper.cookieJar())
                    .build();

            Retrofit retrofit = new Retrofit
                    .Builder()
                    .baseUrl(Endpoint.BASE)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(API_SERVICE_CLASS);
        }
        return apiService;
    }
}

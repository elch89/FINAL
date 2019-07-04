package com.earwormfix.earwormfix.factory;

import com.earwormfix.earwormfix.Rest.VideoUploadApi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoUploadFactory {

    private static final String BASE_URL = "https://earwormfix.com";
    public static VideoUploadApi create() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().
                connectTimeout(120,TimeUnit.SECONDS).
                writeTimeout(120, TimeUnit.SECONDS).
                readTimeout(120,TimeUnit.SECONDS);

        Retrofit retrofit = new retrofit2.Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(VideoUploadApi.class);
    }
}

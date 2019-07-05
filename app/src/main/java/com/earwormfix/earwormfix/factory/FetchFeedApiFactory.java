package com.earwormfix.earwormfix.factory;

import com.earwormfix.earwormfix.Rest.FetchFeedApi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Factory for creating a retrofit instance, and listen to http requests and response
 **/
public class FetchFeedApiFactory {
    private static final String BASE_URL = "https://earwormfix.com";

    public static FetchFeedApi create() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().
                connectTimeout(120,TimeUnit.SECONDS).
                writeTimeout(120, TimeUnit.SECONDS).
                readTimeout(120,TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new retrofit2.Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(FetchFeedApi.class);
    }
}

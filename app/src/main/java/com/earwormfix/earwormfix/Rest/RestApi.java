package com.earwormfix.earwormfix.Rest;

import retrofit2.Call;
import retrofit2.http.GET;


public interface RestApi {


        @GET("stream.php")
        Call<String> fetchVideo(/*@Query("user") String userId*/);
    }

package com.earwormfix.earwormfix.Rest;

import com.earwormfix.earwormfix.Models.Feed;
import com.earwormfix.earwormfix.Models.MyFix;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface FetchFeedApi {

        //https://earwormfix.com/stream.php?id=0&&user=5cfa83882a9316.76570365
        @GET("stream.php")
        Call<List<Feed>> fetchPosts(@Query("page") int page, @Query("page_size") int pageSize, @Query("user") String userId);

        @GET("myfix.php")
        Call<List<MyFix>> getMyPlaylist(@Query("uid") String uid);
    }

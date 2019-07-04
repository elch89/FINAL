package com.earwormfix.earwormfix.Rest;

import com.earwormfix.earwormfix.Models.Post;
import com.earwormfix.earwormfix.Models.MyFix;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface FetchFeedApi {

        @GET("stream.php")
        Call<List<Post>> fetchPosts(@Query("page") int page, @Query("page_size") int pageSize, @Query("user") String userId);

        @GET("myfix.php")
        Call<List<MyFix>> getMyPlaylist(@Query("uid") String uid);
    }

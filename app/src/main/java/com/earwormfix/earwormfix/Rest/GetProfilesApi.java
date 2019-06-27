package com.earwormfix.earwormfix.Rest;

import com.earwormfix.earwormfix.Models.Connectivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetProfilesApi {
    @GET("search.php")
    Call<List<Connectivity>> searchProfiles(@Query("uid") String uid);
}

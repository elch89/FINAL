package com.earwormfix.earwormfix.Rest;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public interface RestApi {
    @Multipart
    @POST("feedback.php")
    Call<ResultObject> sendRequest(@PartMap() Map<String, RequestBody> partMap);
}

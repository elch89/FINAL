package com.earwormfix.earwormfix.Rest;

import com.earwormfix.earwormfix.Models.ResultObject;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public interface DeleteApi {
    @Multipart
    @POST("delete.php")
    Call<ResultObject> deleteItem(@PartMap() Map<String, RequestBody> partMap);
}

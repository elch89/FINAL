package com.earwormfix.earwormfix.Rest;

import com.earwormfix.earwormfix.Models.ResultObject;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
// for general post request, changing value in data base, no need for callback values
public interface RestApi {
    @Multipart
    @POST("feedback.php")
    Call<ResultObject> sendRequest(@PartMap() Map<String, RequestBody> partMap);
}

package com.earwormfix.earwormfix.Rest;

import com.earwormfix.earwormfix.Models.ProfileModel;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface RegisterApi {
    @Multipart
    @POST("register.php")
    Call<ProfileModel> upload(@Part MultipartBody.Part img, @PartMap() Map<String, RequestBody> partMap);
}

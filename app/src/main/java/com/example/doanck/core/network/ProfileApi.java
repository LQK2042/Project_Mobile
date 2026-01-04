package com.example.doanck.core.network;

import com.example.doanck.data.model.ProfileResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProfileApi {

    @GET("api.php?action=profile_get")
    Call<ProfileResponse> getProfile();
}

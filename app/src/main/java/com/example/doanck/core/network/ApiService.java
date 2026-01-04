package com.example.doanck.core.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import com.example.doanck.data.model.LoginRequest;
import com.example.doanck.data.model.LoginResponse;
import com.example.doanck.data.model.DefaultResponse;
import com.example.doanck.data.model.RegisterRequest;
import com.example.doanck.data.model.ForgotPasswordRequest;
import com.example.doanck.data.model.GenericResponse;
import com.example.doanck.data.model.ResetPasswordRequest;
import com.example.doanck.data.model.VerifyOtpRequest;
import com.example.doanck.data.model.VerifyOtpResponse;
import com.example.doanck.data.model.ProductResponse;


public interface ApiService {

    @POST("api.php?action=login")
    Call<LoginResponse> login(@Body LoginRequest req);

    @POST("api.php?action=register")
    Call<DefaultResponse> register(@Body RegisterRequest req);

    // Password reset endpoints
    @POST("api.php")
    Call<GenericResponse> forgotPassword(@Query("action") String action, @Body ForgotPasswordRequest body);

    @POST("api.php")
    Call<VerifyOtpResponse> verifyOtp(@Query("action") String action, @Body VerifyOtpRequest body);

    @POST("api.php")
    Call<GenericResponse> resetPassword(@Query("action") String action, @Body ResetPasswordRequest body);

    // Product endpoints
    @GET("api.php?action=products")
    Call<ProductResponse> getProducts();

}

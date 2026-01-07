package com.example.doanck.core.supabase

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface SupabaseAuthService {

    @POST("signup")
    fun signUp(@Body body: Map<String, Any>): Call<SignUpResponse>

    @POST("token")
    fun signIn(
        @Query("grant_type") grantType: String,
        @Body body: Map<String, Any>
    ): Call<TokenResponse>

    @POST("recover")
    fun recover(@Body body: Map<String, Any>): Call<ResponseBody>

    @PUT("user")
    fun updateUser(
        @Header("Authorization") bearerAccessToken: String,
        @Body body: Map<String, Any>
    ): Call<SbUser>

    @GET("user")
    fun getUser(
        @Header("Authorization") bearerAccessToken: String
    ): Call<UserResponse>
    @POST("verify")
    fun verify(@Body body: Map<String, Any>): Call<TokenResponse>

}

package com.example.doanck.data.remote.supabase

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query


interface SupabaseAuthService {

    @Headers("Content-Type: application/json")
    @POST("token")
    fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body body: SignInRequest
    ): Call<TokenResponse>

    @Headers("Content-Type: application/json")
    @POST("signup")
    fun signUp(
        @Body body: SignUpRequest
    ): Call<SignUpResponse>

    @Headers("Content-Type: application/json")
    @POST("recover")
    fun recover(
        @Body body: RecoverRequest
    ): Call<Unit>

    @Headers("Content-Type: application/json")
    @PUT("user")
    fun updateUser(
        @Header("Authorization") bearerAccessToken: String,
        @Body body: UpdatePasswordRequest
    ): Call<SbUser>

    @GET("user")
    fun getUser(
        @Header("Authorization") bearerAccessToken: String
    ): Call<UserResponse>

    @Headers("Content-Type: application/json")
    @POST("verify")
    fun verify(@Body body: VerifyOtpRequest): Call<TokenResponse>
}

// ========== Request Data Classes ==========

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val data: UserMetadata? = null
)

data class UserMetadata(
    val first_name: String? = null,
    val last_name: String? = null,
    val avatar_url: String? = null
)

data class RecoverRequest(
    val email: String,
    val redirect_to: String? = null
)

data class UpdatePasswordRequest(
    val password: String
)

data class VerifyOtpRequest(
    val type: String,
    val email: String,
    val token: String,
    val redirect_to: String? = null
)

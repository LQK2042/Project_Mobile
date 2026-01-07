package com.example.doanck.core.supabase

import com.google.gson.annotations.SerializedName

data class SbUser(
    @SerializedName("id") val id: String?,
    @SerializedName("email") val email: String?
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Long?,
    @SerializedName("user") val user: SbUser?
)

data class Session(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Long?
)

data class SignUpResponse(
    @SerializedName("user") val user: SbUser?,
    @SerializedName("session") val session: Session?
)

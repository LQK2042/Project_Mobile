package com.example.doanck.data.remote.supabase

import com.example.doanck.domain.model.UserProfile
import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("role") val role: String?
)

fun ProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    fullName = fullName,
    phone = phone,
    avatarUrl = avatarUrl,
    role = role
)

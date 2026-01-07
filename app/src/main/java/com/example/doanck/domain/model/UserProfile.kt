package com.example.doanck.domain.model

data class UserProfile(
    val id: String,
    val fullName: String?,
    val phone: String?,
    val avatarUrl: String?,
    val role: String?
)

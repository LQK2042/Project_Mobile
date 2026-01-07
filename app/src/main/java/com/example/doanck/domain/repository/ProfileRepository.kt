package com.example.doanck.domain.repository

import com.example.doanck.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(uid: String): UserProfile?
}

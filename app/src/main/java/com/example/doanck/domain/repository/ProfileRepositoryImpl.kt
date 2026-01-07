package com.example.doanck.domain.repository

import com.example.doanck.data.remote.supabase.ProfileApi
import com.example.doanck.data.remote.supabase.toDomain
import com.example.doanck.domain.model.UserProfile

class ProfileRepositoryImpl(
    private val api: ProfileApi
) : ProfileRepository {

    override suspend fun getProfile(uid: String): UserProfile? {
        return api.getProfileById(idFilter = "eq.$uid").firstOrNull()?.toDomain()
    }
}

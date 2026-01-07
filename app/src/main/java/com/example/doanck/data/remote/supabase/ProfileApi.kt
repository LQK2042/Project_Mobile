package com.example.doanck.data.remote.supabase

import retrofit2.http.GET
import retrofit2.http.Query

interface ProfileApi {
    // Supabase PostgREST filter: id=eq.<uuid>
    @GET("profiles")
    suspend fun getProfileById(
        @Query("select") select: String = "id,full_name,phone,avatar_url,role",
        @Query("id") idFilter: String
    ): List<ProfileDto>
}

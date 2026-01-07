package com.example.doanck.di

import android.content.Context
import com.example.doanck.data.remote.supabase.AuthStore
import okhttp3.Interceptor
import okhttp3.Response

class SupabaseAuthInterceptor(
    private val context: Context,
    private val anonKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = AuthStore.accessToken(context)
        val bearer = if (!accessToken.isNullOrBlank()) accessToken else anonKey

        val req = chain.request().newBuilder()
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $bearer")
            .header("Accept", "application/json")
            .build()

        return chain.proceed(req)
    }
}

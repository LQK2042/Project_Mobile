package com.example.doanck.di

import okhttp3.Interceptor
import okhttp3.Response

class SupabaseAuthInterceptor(
    private val anonKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .header("Accept", "application/json")
            .build()

        return chain.proceed(req)
    }
}

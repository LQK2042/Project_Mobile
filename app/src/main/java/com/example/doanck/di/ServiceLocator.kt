package com.example.doanck.di

import com.example.doanck.data.remote.supabase.SupabaseRemoteDataSource
import com.example.doanck.data.remote.supabase.SupabaseService
import com.example.doanck.domain.repository.ShopRepository
import com.example.doanck.domain.repository.ShopRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {

    private const val BASE_URL = "https://qjatgukztpwjvyuxwfoe.supabase.co/rest/v1/"
    private const val SUPABASE_ANON_KEY = "sb_publishable__sXepTd-mhK3v8M0svq4JQ_FMkMwDjF"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(SupabaseAuthInterceptor(SUPABASE_ANON_KEY))
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val supabaseService: SupabaseService =
        retrofit.create(SupabaseService::class.java)

    private val remote = SupabaseRemoteDataSource(supabaseService)

    val shopRepository: ShopRepository = ShopRepositoryImpl(remote)
}

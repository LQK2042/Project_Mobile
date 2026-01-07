package com.example.doanck.di

import android.content.Context
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.data.remote.supabase.SupabaseConfig
import com.example.doanck.data.remote.supabase.SupabaseRemoteDataSource
import com.example.doanck.data.remote.supabase.SupabaseService
import com.example.doanck.domain.repository.ShopRepository
import com.example.doanck.domain.repository.ShopRepositoryImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {

    private const val BASE_URL = "https://qjatgukztpwjvyuxwfoe.supabase.co/rest/v1/"

    private val logging by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttp by lazy {
        OkHttpClient.Builder()
            .addInterceptor(SupabaseAuthInterceptor(SupabaseConfig.SUPABASE_KEY))
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val supabaseService: SupabaseService by lazy {
        retrofit.create(SupabaseService::class.java)
    }

    private val remote by lazy { SupabaseRemoteDataSource(supabaseService) }

    val shopRepository: ShopRepository by lazy { ShopRepositoryImpl(remote) }

    class SupabaseHeadersInterceptor(
        private val context: Context
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val token = AuthStore.accessToken(context)

            val req = chain.request().newBuilder()
                .header("apikey", SupabaseConfig.SUPABASE_KEY)
                .apply {
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    } else {
                        removeHeader("Authorization")
                    }
                }
                .build()

            return chain.proceed(req)
        }
    }
}

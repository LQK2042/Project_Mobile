package com.example.doanck.di

import android.content.Context
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.data.remote.supabase.SupabaseConfig
import com.example.doanck.data.remote.supabase.SupabaseRemoteDataSource
import com.example.doanck.data.remote.supabase.SupabaseService
import com.example.doanck.data.remote.supabase.ProfileApi
import com.example.doanck.domain.repository.ShopRepository
import com.example.doanck.domain.repository.ShopRepositoryImpl
import com.example.doanck.domain.repository.ProfileRepository
import com.example.doanck.domain.repository.ProfileRepositoryImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {

    private const val BASE_URL = "https://qjatgukztpwjvyuxwfoe.supabase.co/rest/v1/"

    // ✅ cần context để đọc AuthStore token
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val logging by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttp by lazy {
        OkHttpClient.Builder()
            // ✅ DÙNG interceptor này thay vì SupabaseAuthInterceptor cũ
            .addInterceptor(SupabaseHeadersInterceptor(appContext, SupabaseConfig.SUPABASE_KEY))
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

    // ===== Profile =====
    val profileApi: ProfileApi by lazy {
        retrofit.create(ProfileApi::class.java)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(profileApi)
    }

    // ✅ Interceptor đúng: có token => Bearer token, không có => Bearer anonKey
    class SupabaseHeadersInterceptor(
        private val context: Context,
        private val anonKey: String
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val token = AuthStore.accessToken(context)
            val bearer = if (!token.isNullOrBlank()) token else anonKey

            val req = chain.request().newBuilder()
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $bearer")
                .header("Accept", "application/json")
                .build()

            return chain.proceed(req)
        }
    }
}

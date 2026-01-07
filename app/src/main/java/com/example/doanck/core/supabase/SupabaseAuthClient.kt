package com.example.doanck.core.supabase

import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseAuthClient {

    val service: SupabaseAuthService by lazy {
        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val ok = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val b: Request.Builder = original.newBuilder()
                    .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")

                // default Authorization nếu chưa có
                if (original.header("Authorization") == null) {
                    b.header("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                }
                chain.proceed(b.build())
            })
            .addInterceptor(log)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("${SupabaseConfig.SUPABASE_URL}/auth/v1/")
            .client(ok)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseAuthService::class.java)
    }

    fun parseError(response: Response<*>): String {
        return try {
            val s = response.errorBody()?.string().orEmpty()
            val o = JsonParser.parseString(s).asJsonObject
            when {
                o.has("error_description") -> o["error_description"].asString
                o.has("msg") -> o["msg"].asString
                o.has("message") -> o["message"].asString
                else -> if (s.isNotBlank()) s else "Request failed"
            }
        } catch (_: Exception) {
            "Request failed"
        }
    }
}

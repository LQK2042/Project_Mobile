package com.example.doanck.di

import com.example.doanck.data.remote.mock.DummyRemoteDataSource
import com.example.doanck.data.remote.mock.DummyService
import com.example.doanck.domain.repository.ShopRepositoryImpl
import com.example.doanck.domain.repository.ShopRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {
    private const val BASE_URL = "https://dummyjson.com/"

    private val okHttp = OkHttpClient.Builder().build()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val dummyService: DummyService = retrofit.create(DummyService::class.java)

    private val remote = DummyRemoteDataSource(dummyService)

    val shopRepository: ShopRepository = ShopRepositoryImpl(remote)
}

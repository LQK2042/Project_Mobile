package com.example.doanck.data.remote.mock

import com.example.doanck.data.remote.mock.dto.ProductsResponseDto
import com.example.doanck.data.remote.mock.dto.UsersResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface DummyService {

    @GET("users")
    suspend fun getUsers(
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): UsersResponseDto

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): ProductsResponseDto
}

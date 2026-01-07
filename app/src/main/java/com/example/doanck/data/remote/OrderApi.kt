package com.example.doanck.data.remote

import com.example.doanck.data.remote.dto.CreateOrderItemRequest
import com.example.doanck.data.remote.dto.CreateOrderRequest
import com.example.doanck.data.remote.dto.CreateOrderResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderApi {

    /**
     * Tạo order mới trong table `orders`
     * Header "Prefer: return=representation" để Supabase trả về record vừa tạo
     */
    @POST("orders")
    suspend fun createOrder(
        @Header("Prefer") prefer: String = "return=representation",
        @Body req: CreateOrderRequest
    ): List<CreateOrderResponse>

    /**
     * Tạo order_items trong table `order_items`
     */
    @POST("order_items")
    suspend fun createOrderItems(
        @Body items: List<CreateOrderItemRequest>
    )

    /**
     * Lấy thông tin order theo id
     * Supabase REST trả về array nên cần filter bằng query param
     */
    @GET("orders")
    suspend fun getOrder(
        @Query("id") orderId: String,
        @Query("select") select: String = "*"
    ): List<OrderDetailResponse>
}

data class OrderDetailResponse(
    @SerializedName("id")
    val orderId: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("total")
    val total: Long
)


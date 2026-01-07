package com.example.doanck.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request tạo order trong table `orders`
 * Không chứa items - items sẽ được tạo riêng trong table `order_items`
 */
data class CreateOrderRequest(
    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("payment_status")
    val paymentStatus: String = "unpaid",

    @SerializedName("subtotal")
    val subtotal: Long,

    @SerializedName("delivery_fee")
    val deliveryFee: Long = 0,

    @SerializedName("total")
    val total: Long
)

/**
 * Request tạo order_item trong table `order_items`
 */
data class CreateOrderItemRequest(
    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("product_id")
    val productId: String,

    @SerializedName("product_name")
    val productName: String,

    @SerializedName("unit_price")
    val unitPrice: Long,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("line_total")
    val lineTotal: Long
)

/**
 * Response khi tạo order (Supabase trả về array)
 */
data class CreateOrderResponse(
    @SerializedName("id")
    val orderId: String,

    @SerializedName("status")
    val status: String
)

// Giữ lại cho tương thích
data class CreateOrderItem(
    val productId: String,
    val quantity: Int,
    val price: Long
)


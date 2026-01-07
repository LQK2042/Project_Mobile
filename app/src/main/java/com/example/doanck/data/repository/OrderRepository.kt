package com.example.doanck.data.repository

import android.content.Context
import com.example.doanck.data.local.CartItemEntity
import com.example.doanck.data.remote.OrderApi
import com.example.doanck.data.remote.OrderDetailResponse
import com.example.doanck.data.remote.dto.CreateOrderItemRequest
import com.example.doanck.data.remote.dto.CreateOrderRequest
import com.example.doanck.data.remote.supabase.AuthStore

class OrderRepository(
    private val api: OrderApi,
    private val context: Context
) {

    /**
     * Tạo đơn hàng mới:
     * 1. Tạo record trong table `orders`
     * 2. Tạo các records trong table `order_items`
     */
    suspend fun createOrder(items: List<CartItemEntity>): String {
        val userId = AuthStore.userId(context) ?: error("User not logged in")
        val shopId = items.first().shopId

        // Tính tổng tiền
        val subtotal = items.sumOf { it.price * it.quantity }
        val deliveryFee = 0L // Có thể tính phí ship sau
        val total = subtotal + deliveryFee

        // 1. Tạo order
        val orderReq = CreateOrderRequest(
            shopId = shopId,
            userId = userId,
            status = "pending",
            paymentStatus = "unpaid",
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            total = total
        )

        val orderResponse = api.createOrder(req = orderReq)
        if (orderResponse.isEmpty()) {
            error("Tạo đơn hàng thất bại - không có response")
        }

        val orderId = orderResponse.first().orderId

        // 2. Tạo order_items
        val orderItems = items.map { item ->
            CreateOrderItemRequest(
                orderId = orderId,
                productId = item.productId,
                productName = item.name,
                unitPrice = item.price,
                quantity = item.quantity,
                lineTotal = item.price * item.quantity
            )
        }

        api.createOrderItems(orderItems)

        return orderId
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     */
    suspend fun getOrder(orderId: String): OrderDetailResponse {
        val response = api.getOrder(orderId = "eq.$orderId")
        if (response.isEmpty()) {
            error("Không tìm thấy đơn hàng")
        }
        return response.first()
    }
}


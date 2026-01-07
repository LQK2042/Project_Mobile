package com.example.doanck.ui.cart

import com.example.doanck.data.local.CartItemEntity

data class CartUiState(
    val items: List<CartItemEntity> = emptyList(),
    val total: Long = 0L
)

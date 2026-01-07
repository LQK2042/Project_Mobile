package com.example.doanck.domain.model

data class ProductSuggest(
    val productId: String,
    val name: String,
    val price: Int,
    val imageUrl: String?,
    val shopId: String,
    val shopName: String,
    val shopLogoUrl: String?
)

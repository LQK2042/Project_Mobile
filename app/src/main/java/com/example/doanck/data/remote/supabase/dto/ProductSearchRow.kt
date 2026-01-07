package com.example.doanck.data.remote.supabase.dto

data class ShopMiniRow(
    val id: String,
    val name: String,
    val logo_url: String?
)

data class ProductSearchRow(
    val id: String,
    val name: String,
    val price: Int,
    val image_url: String?,
    val shop_id: String,
    val shops: ShopMiniRow? // embed
)

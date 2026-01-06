package com.example.doanck.data.remote.supabase.dto

// bảng shops: id uuid, name text, address text, phone text, rating numeric, logo_url text
data class ShopRowDto(
    val id: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val rating: Double? = null,
    val logo_url: String? = null
)

// bảng products: id uuid, shop_id uuid, name text, price int4, image_url text
data class ProductRowDto(
    val id: String,
    val shop_id: String,
    val name: String,
    val price: Int,
    val image_url: String? = null
)

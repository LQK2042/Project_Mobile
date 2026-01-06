package com.example.doanck.data.remote.supabase.dto

import com.google.gson.annotations.SerializedName

data class ShopRow(
    val id: String,
    val name: String,
    val address: String?,
    val phone: String?,
    val rating: Double?,
    @SerializedName("logo_url") val logo_url: String?
)

data class ProductRow(
    val id: String,
    val name: String,
    val price: Int,
    @SerializedName("image_url") val image_url: String?,
    @SerializedName("shop_id") val shop_id: String,
    @SerializedName("is_available") val is_available: Boolean,
    @SerializedName("created_at") val created_at: String?
)

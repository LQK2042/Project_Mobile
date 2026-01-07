package com.example.doanck.data.remote.supabase

import com.example.doanck.data.remote.supabase.dto.ProductRow
import com.example.doanck.data.remote.supabase.dto.ShopRow
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseService {

    @GET("shops")
    suspend fun getShops(
        @Query("select") select: String = "id,name,logo_url,rating,address",
        @Query("is_active") isActive: String = "eq.true",
        @Query("order") order: String = "rating.desc.nullslast,created_at.desc",
        @Query("limit") limit: Int = 5
    ): List<ShopDto>
    data class ShopDto(
        val id: String,
        val name: String,
        val logo_url: String?,
        val rating: Double?,
        val address: String?,
        val phone: String?
    )
    fun extractArea(address: String?): String {
        if (address.isNullOrBlank()) return ""
        val parts = address.split(",")
        return parts.lastOrNull()?.trim().orEmpty()
    }

    @GET("products")
    suspend fun getProductsByShop(
        @Query("select") select: String = "id,name,price,image_url,shop_id,is_available,created_at",
        @Query("shop_id") shopIdFilter: String,
        @Query("is_available") isAvailable: String? = null,
        @Query("order") order: String? = null
    ): List<ProductRow>
}

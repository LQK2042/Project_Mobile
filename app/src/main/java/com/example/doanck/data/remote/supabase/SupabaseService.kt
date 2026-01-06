package com.example.doanck.data.remote.supabase

import com.example.doanck.data.remote.supabase.dto.ProductRow
import com.example.doanck.data.remote.supabase.dto.ShopRow
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseService {

    @GET("shops")
    suspend fun getShops(
        @Query("select") select: String = "id,name,address,phone,rating,logo_url"
    ): List<ShopRow>

    @GET("products")
    suspend fun getProductsByShop(
        @Query("select") select: String = "id,name,price,image_url,shop_id,is_available,created_at",
        @Query("shop_id") shopIdFilter: String,
        @Query("is_available") isAvailable: String? = null,
        @Query("order") order: String? = null
    ): List<ProductRow>
}

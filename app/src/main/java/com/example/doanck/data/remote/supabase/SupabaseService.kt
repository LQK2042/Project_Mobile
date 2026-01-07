package com.example.doanck.data.remote.supabase

import com.example.doanck.data.remote.supabase.dto.ProductRow
import com.example.doanck.data.remote.supabase.dto.ProductSearchRow
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
        @Query("shop_id") shopIdFilter: String,            // "eq.<uuid>"
        @Query("is_available") isAvailable: String? = null, // "eq.true"
        @Query("order") order: String? = null              // "created_at.desc"
    ): List<ProductRow>

    // ✅ search theo tên product
    @GET("products")
    suspend fun searchProducts(
        @Query("select") select: String =
            "id,name,price,image_url,shop_id,shops(id,name,logo_url)",
        @Query("name") nameFilter: String,                 // "ilike.*pho*"
        @Query("is_available") isAvailable: String = "eq.true",
        @Query("limit") limit: Int = 20,
        @Query("order") order: String = "created_at.desc"
    ): List<ProductSearchRow>
}

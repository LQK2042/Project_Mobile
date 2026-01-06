package com.example.doanck.data.remote.supabase

import com.example.doanck.data.remote.supabase.dto.ProductRowDto
import com.example.doanck.data.remote.supabase.dto.ShopRowDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseService {

    @GET("shops")
    suspend fun getShops(
        @Query("select") select: String = "id,name,address,phone,rating,logo_url"
    ): List<ShopRowDto>

    @GET("products")
    suspend fun getProductsByShop(
        @Query("select") select: String = "id,shop_id,name,price,image_url",
        @Query("shop_id") shopIdEq: String // MUST be "eq.<uuid>"
    ): List<ProductRowDto>
}

package com.example.doanck.data.remote.supabase

import android.util.Log
import com.example.doanck.data.remote.common.RemoteDataSource
import com.example.doanck.domain.model.Product
import com.example.doanck.domain.model.Shop


class SupabaseRemoteDataSource(
    private val service: SupabaseService
) : RemoteDataSource {

    override suspend fun getShops(): List<Shop> {
        val rows = service.getShops()
        return rows.map {
            Shop(
                id = it.id,
                name = it.name,
                address = it.address,
                phone = it.phone,
                rating = it.rating,
                logoUrl = it.logo_url
            )
        }
    }

    override suspend fun getProducts(shopId: Int): List<Product> {
        Log.d("API", "loadProducts shopId=$shopId")
//        require(shopId.isNotBlank()) { "shopId is blank" }

        val rows = service.getProductsByShop(shopIdEq = "eq.$shopId")
        return rows.map {
            Product(
                id = it.id,
                name = it.name,
                price = it.price,
                imageUrl = it.image_url
            )
        }
    }
}

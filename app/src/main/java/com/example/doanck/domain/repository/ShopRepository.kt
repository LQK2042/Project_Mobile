package com.example.doanck.domain.repository
import com.example.doanck.domain.model.Product
import com.example.doanck.domain.model.Shop

interface ShopRepository {
    suspend fun getShops(): List<Shop>
    suspend fun getProducts(shopId: Int): List<Product>
}

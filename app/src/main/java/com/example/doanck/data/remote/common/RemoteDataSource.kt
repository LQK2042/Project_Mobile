package com.example.doanck.data.remote.common

import com.example.doanck.domain.model.Product
import com.example.doanck.domain.model.Shop

interface RemoteDataSource {
    suspend fun getShops(): List<Shop>
    suspend fun getProducts(shopId: Int): List<Product>
}

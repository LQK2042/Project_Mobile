package com.example.doanck.data.remote.common

import com.example.doanck.domain.model.Product
import com.example.doanck.domain.model.Shop
import com.example.doanck.domain.model.ProductSuggest

interface RemoteDataSource {
    suspend fun getShops(): List<Shop>
    suspend fun getProducts(shopId: String): List<Product>
    suspend fun searchProducts(keyword: String): List<ProductSuggest>

}

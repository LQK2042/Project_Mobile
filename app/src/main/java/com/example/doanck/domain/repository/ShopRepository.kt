package com.example.doanck.domain.repository
import com.example.doanck.domain.model.Product
import com.example.doanck.domain.model.Shop
import com.example.doanck.domain.model.ProductSuggest
interface ShopRepository {
    suspend fun getShops(): List<Shop>
    suspend fun getProducts(shopId: String): List<Product>
    suspend fun searchProducts(keyword: String): List<ProductSuggest>

}

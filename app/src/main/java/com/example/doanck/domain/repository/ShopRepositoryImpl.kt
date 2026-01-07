package com.example.doanck.domain.repository

import com.example.doanck.data.remote.common.RemoteDataSource
import com.example.doanck.domain.model.Product
import com.example.doanck.domain.model.Shop
import com.example.doanck.domain.model.ProductSuggest
class ShopRepositoryImpl(
    private val remote: RemoteDataSource
) : ShopRepository {
    override suspend fun getShops(): List<Shop> = remote.getShops()
    override suspend fun getProducts(shopId: String): List<Product> {
        return remote.getProducts(shopId)
    }
    override suspend fun searchProducts(keyword: String) =
        remote.searchProducts(keyword)

}

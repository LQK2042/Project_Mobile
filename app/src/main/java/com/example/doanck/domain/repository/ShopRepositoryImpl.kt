package com.example.doanck.domain.repository

import com.example.doanck.data.remote.common.RemoteDataSource
import com.example.doanck.domain.repository.ShopRepository

class ShopRepositoryImpl(
    private val remote: RemoteDataSource
) : ShopRepository {
    override suspend fun getShops() = remote.getShops()
    override suspend fun getProducts(shopId: Int) = remote.getProducts(shopId)
}

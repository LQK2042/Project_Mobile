package com.example.doanck.data.repository

import com.example.doanck.data.local.CartDao
import com.example.doanck.data.local.CartItemEntity
import com.example.doanck.domain.model.Product
import kotlinx.coroutines.flow.Flow

class CartRepository(private val dao: CartDao) {

    fun observeCart(): Flow<List<CartItemEntity>> = dao.observeCart()

    suspend fun add(product: Product) {
        ensureSingleShop(product.shopId)

        val current = dao.getById(product.id)
        val newQty = (current?.quantity ?: 0) + 1
        dao.upsert(
            CartItemEntity(
                productId = product.id,
                shopId = product.shopId,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrl,
                quantity = newQty
            )
        )
    }

    suspend fun minus(product: Product) {
        val current = dao.getById(product.id) ?: return
        val newQty = current.quantity - 1
        if (newQty <= 0) dao.delete(product.id)
        else dao.upsert(current.copy(quantity = newQty))
    }

    suspend fun clear() = dao.clearAll()

    private suspend fun ensureSingleShop(newShopId: String) {
        val shopIds = dao.distinctShopIds()
        if (shopIds.isNotEmpty() && shopIds.any { it != newShopId }) {
            dao.clearAll()
        }
    }
}

package com.example.doanck.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun observeCart(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :pid LIMIT 1")
    suspend fun getById(pid: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :pid")
    suspend fun delete(pid: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearAll()

    @Query("SELECT DISTINCT shopId FROM cart_items")
    suspend fun distinctShopIds(): List<String>
}


package com.example.doanck.data.remote.supabase

import android.util.Log
import com.example.doanck.data.remote.common.RemoteDataSource
import com.example.doanck.domain.model.Product
import com.example.doanck.domain.model.ProductSuggest
import com.example.doanck.domain.model.Shop
import retrofit2.HttpException
import java.io.IOException

class SupabaseRemoteDataSource(
    private val service: SupabaseService
) : RemoteDataSource {

    override suspend fun getShops(): List<Shop> = try {
        val rows = service.getShops()
        rows.map {
            Shop(
                id = it.id,
                name = it.name,
                address = it.address,
                phone = it.phone,
                rating = it.rating,
                logoUrl = it.logo_url
            )
        }
    } catch (e: HttpException) {
        Log.e("API", "getShops HTTP ${e.code()} body=${e.response()?.errorBody()?.string()}", e)
        emptyList()
    } catch (e: IOException) {
        Log.e("API", "getShops network error: ${e.localizedMessage}", e)
        emptyList()
    }

    override suspend fun getProducts(shopId: String): List<Product> = try {
        val rows = service.getProductsByShop(
            shopIdFilter = "eq.$shopId",
            isAvailable = "eq.true",
            order = "created_at.desc"
        )
        rows.map {
            Product(
                id = it.id,
                name = it.name,
                price = it.price.toLong(),
                imageUrl = it.image_url,
                shopId = it.shop_id
            )
        }
    } catch (e: HttpException) {
        Log.e("API", "getProducts HTTP ${e.code()} body=${e.response()?.errorBody()?.string()}", e)
        emptyList()
    } catch (e: IOException) {
        Log.e("API", "getProducts network error: ${e.localizedMessage}", e)
        emptyList()
    }

    override suspend fun searchProducts(keyword: String): List<ProductSuggest> {
        return try {
            val key = keyword.trim()
            if (key.isEmpty()) {
                emptyList()
            } else {
                val rows = service.searchProducts(
                    nameFilter = "ilike.*$key*"
                )

                rows.mapNotNull { r ->
                    val shop = r.shops ?: return@mapNotNull null
                    ProductSuggest(
                        productId = r.id,
                        name = r.name,
                        price = r.price,
                        imageUrl = r.image_url,
                        shopId = r.shop_id,
                        shopName = shop.name,
                        shopLogoUrl = shop.logo_url
                    )
                }
            }
        } catch (e: HttpException) {
            Log.e("API", "searchProducts HTTP ${e.code()} body=${e.response()?.errorBody()?.string()}", e)
            emptyList()
        } catch (e: IOException) {
            Log.e("API", "searchProducts network error: ${e.localizedMessage}", e)
            emptyList()
        }
    }

}

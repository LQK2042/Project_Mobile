//package com.example.doanck.data.remote.mock
//
//import com.example.doanck.data.remote.common.RemoteDataSource
//import com.example.doanck.domain.model.Product
//import com.example.doanck.domain.model.Shop
//
//class SupabaseRemoteDataSource(
//    private val service: DummyService
//) : RemoteDataSource {
//
//    override suspend fun getShops(): List<Shop> {
//        val res = service.getUsers(limit = 20)
//        return res.users.map { u ->
//            Shop(
//                id = u.id,
//                name = "${u.firstName} ${u.lastName} Store",
//                address = u.address.address
//            )
//        }
//    }
//
//    override suspend fun getProducts(shopId: String): List<Product> {
//        // DummyJSON không có products theo shop => giả lập bằng skip
//        val skip = (shopId * 20) % 80
//        val res = service.getProducts(limit = 20, skip = skip)
//        return res.products.map { p ->
//            Product(
//                id = p.id,
//                name = p.title,
//                price = p.price,
//                imageUrl = p.thumbnail
//            )
//        }
//    }
//}

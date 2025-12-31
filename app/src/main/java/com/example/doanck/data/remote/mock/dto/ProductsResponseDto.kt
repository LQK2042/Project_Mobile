package com.example.doanck.data.remote.mock.dto
data class ProductsResponseDto(val products: List<ProductDto>)

data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val thumbnail: String?
)

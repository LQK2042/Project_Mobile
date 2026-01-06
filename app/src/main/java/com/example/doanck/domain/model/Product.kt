package com.example.doanck.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Int,
    val imageUrl: String? = null
)


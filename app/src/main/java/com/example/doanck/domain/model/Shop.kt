package com.example.doanck.domain.model

data class Shop(
    val id: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val rating: Double? = null,
    val logoUrl: String? = null
)



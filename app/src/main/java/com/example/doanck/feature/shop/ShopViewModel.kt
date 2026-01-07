package com.example.doanck.feature.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.doanck.data.local.AppDatabase
import com.example.doanck.data.repository.CartRepository
import com.example.doanck.di.ServiceLocator
import com.example.doanck.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ServiceLocator.shopRepository
    private val cartRepo = CartRepository(AppDatabase.getInstance(application).cartDao())

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    // Observe cart để cập nhật số lượng trên UI
    val cartQuantities = cartRepo.observeCart().map { items ->
        items.associate { it.productId to it.quantity }
    }

    fun loadProducts(shopId: String) {
        viewModelScope.launch {
            _products.value = repo.getProducts(shopId)
        }
    }

    fun onPlus(product: Product) {
        viewModelScope.launch {
            cartRepo.add(product)
        }
    }

    fun onMinus(product: Product) {
        viewModelScope.launch {
            cartRepo.minus(product)
        }
    }
}


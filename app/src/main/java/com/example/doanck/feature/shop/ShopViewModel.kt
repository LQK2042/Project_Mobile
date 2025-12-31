package com.example.doanck.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doanck.di.ServiceLocator
import com.example.doanck.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {

    private val repo = ServiceLocator.shopRepository

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    fun loadProducts(shopId: Int) {
        viewModelScope.launch {
            _products.value = repo.getProducts(shopId)
        }
    }
}

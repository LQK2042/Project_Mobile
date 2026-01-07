package com.example.doanck.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doanck.di.ServiceLocator
import com.example.doanck.domain.model.Shop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.doanck.domain.model.ProductSuggest
class HomeViewModel : ViewModel() {
    private val repo = ServiceLocator.shopRepository

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    fun load() {
        viewModelScope.launch {
            _shops.value = repo.getShops()
        }
    }
    private val _suggestions = MutableStateFlow<List<ProductSuggest>>(emptyList())
    val suggestions: StateFlow<List<ProductSuggest>> = _suggestions

    suspend fun doSearch(keyword: String) {
        val key = keyword.trim()
        _suggestions.value =
            if (key.length < 2) emptyList()
            else repo.searchProducts(key)
    }

}

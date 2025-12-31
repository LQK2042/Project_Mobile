package com.example.doanck.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doanck.di.ServiceLocator
import com.example.doanck.domain.model.Shop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repo = ServiceLocator.shopRepository

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    fun load() {
        viewModelScope.launch {
            _shops.value = repo.getShops()
        }
    }
}

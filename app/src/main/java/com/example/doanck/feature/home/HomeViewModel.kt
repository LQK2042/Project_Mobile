package com.example.doanck.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doanck.data.remote.supabase.SupabaseService
import com.example.doanck.di.ServiceLocator
import com.example.doanck.core.utils.extractArea
import com.example.doanck.core.utils.normalizeArea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val api: SupabaseService = ServiceLocator.supabaseService  // ✅ bạn cần expose cái này ở ServiceLocator

    private val _shops = MutableStateFlow<List<ShopUi>>(emptyList())
    val shops: StateFlow<List<ShopUi>> = _shops

    fun load() = viewModelScope.launch {
        val list = api.getShops(limit = 5) // ✅ top 5
        _shops.value = list.map {
            val area = normalizeArea(extractArea(it.address))
            ShopUi(
                id = it.id,
                name = it.name,
                logoUrl = it.logo_url,
                rating = it.rating ?: 0.0,
                area = area
            )
        }
    }
}

data class ShopUi(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val rating: Double,
    val area: String
)

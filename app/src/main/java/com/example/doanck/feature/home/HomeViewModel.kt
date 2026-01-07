package com.example.doanck.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.data.repository.ProfileRepository
import com.example.doanck.di.ServiceLocator
import com.example.doanck.domain.model.Shop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.doanck.domain.model.ProductSuggest

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ServiceLocator.shopRepository
    private val profileRepo = ProfileRepository(application)

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    // Avatar URL của user hiện tại
    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl

    fun load() {
        viewModelScope.launch {
            _shops.value = repo.getShops()
        }
    }

    /**
     * Load avatar từ profile (nếu đã login)
     */
    fun loadAvatar() {
        if (!AuthStore.isLoggedIn(getApplication())) {
            _avatarUrl.value = null
            return
        }
        viewModelScope.launch {
            try {
                val profile = profileRepo.getMyProfile()
                _avatarUrl.value = profile.avatarUrl
            } catch (e: Exception) {
                _avatarUrl.value = null
            }
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

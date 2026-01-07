package com.example.doanck.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doanck.di.ServiceLocator
import com.example.doanck.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val repo = ServiceLocator.profileRepository

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    fun load(uid: String) {
        if (uid.isBlank()) {
            _state.value = ProfileUiState(error = "Thiếu userId")
            return
        }

        viewModelScope.launch {
            _state.value = ProfileUiState(loading = true)
            runCatching { repo.getProfile(uid) }
                .onSuccess { p ->
                    _state.value = ProfileUiState(profile = p)
                }
                .onFailure { e ->
                    _state.value = ProfileUiState(error = e.message ?: "Lỗi tải profile")
                }
        }
    }
}

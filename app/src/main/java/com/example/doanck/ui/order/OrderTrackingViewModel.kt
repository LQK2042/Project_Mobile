package com.example.doanck.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doanck.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OrderTrackingUiState(
    val orderId: String = "",
    val status: String = "pending",
    val total: Long = 0L,
    val loading: Boolean = true,
    val error: String? = null
)

class OrderTrackingViewModel(
    private val repo: OrderRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(OrderTrackingUiState())
    val ui: StateFlow<OrderTrackingUiState> = _ui

    fun load(orderId: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(orderId = orderId, loading = true, error = null)
            try {
                val res = repo.getOrder(orderId)
                _ui.value = _ui.value.copy(
                    status = res.status,
                    total = res.total,
                    loading = false
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = e.message ?: "Load đơn thất bại"
                )
            }
        }
    }

    class Factory(private val repo: OrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrderTrackingViewModel(repo) as T
        }
    }
}

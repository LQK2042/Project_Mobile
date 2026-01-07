package com.example.doanck.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doanck.auth.SessionManager
import com.example.doanck.data.local.CartItemEntity
import com.example.doanck.domain.model.Product
import com.example.doanck.data.repository.CartRepository
import com.example.doanck.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepo: CartRepository,
    private val orderRepo: OrderRepository,
    private val session: SessionManager
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = cartRepo.observeCart()
        .map { list ->
            val total = list.sumOf { it.price * it.quantity }
            CartUiState(items = list, total = total)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartUiState())

    /** Kiểm tra user đã đăng nhập chưa */
    fun isLoggedIn(): Boolean = session.isLoggedIn()

    fun plus(item: CartItemEntity) = viewModelScope.launch {
        cartRepo.add(item.toProduct())
    }

    fun minus(item: CartItemEntity) = viewModelScope.launch {
        cartRepo.minus(item.toProduct())
    }

    /**
     * STEP 4 + 5: guest -> báo require login, login -> create order
     */
    fun checkout(
        onRequireLogin: () -> Unit,
        onOrderCreated: (orderId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (!session.isLoggedIn()) {
                onRequireLogin()
                return@launch
            }

            val items = uiState.value.items
            if (items.isEmpty()) {
                onError("Giỏ hàng trống")
                return@launch
            }

            try {
                // STEP 5: gọi API tạo order
                val orderId = orderRepo.createOrder(items)
                // clear cart sau khi tạo thành công
                cartRepo.clear()
                onOrderCreated(orderId)
            } catch (e: Exception) {
                onError(e.message ?: "Tạo đơn thất bại")
            }
        }
    }

    private fun CartItemEntity.toProduct(): Product = Product(
        id = productId,
        shopId = shopId,
        name = name,
        price = price,
        imageUrl = imageUrl
    )

    class Factory(
        private val cartRepo: CartRepository,
        private val orderRepo: OrderRepository,
        private val session: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CartViewModel(cartRepo, orderRepo, session) as T
        }
    }
}


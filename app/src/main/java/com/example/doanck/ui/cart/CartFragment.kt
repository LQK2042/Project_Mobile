package com.example.doanck.ui.cart

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
import com.example.doanck.auth.SessionManager
import com.example.doanck.data.local.AppDatabase
import com.example.doanck.data.local.OrderStore
import com.example.doanck.data.repository.CartRepository
import com.example.doanck.data.repository.OrderRepository
import com.example.doanck.di.ServiceLocator
import kotlinx.coroutines.launch

class CartFragment : Fragment(R.layout.fragment_cart) {

    private val vm: CartViewModel by viewModels {
        val db = AppDatabase.getInstance(requireContext())
        val cartRepo = CartRepository(db.cartDao())
        val orderRepo = OrderRepository(ServiceLocator.orderApi, requireContext())
        val session = SessionManager(requireContext())
        CartViewModel.Factory(cartRepo, orderRepo, session)
    }

    private lateinit var adapter: CartAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var btnCheckout: Button
    private lateinit var btnBack: ImageButton

    // Cờ đánh dấu đang chờ checkout sau khi login
    private var pendingCheckoutAfterLogin = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        tvTotal = view.findViewById(R.id.tvTotal)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        btnCheckout = view.findViewById(R.id.btnCheckout)
        btnBack = view.findViewById(R.id.btnBack)

        adapter = CartAdapter(
            onPlus = { vm.plus(it) },
            onMinus = { vm.minus(it) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Lắng nghe kết quả login từ LoginFragment
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("login_success")?.observe(viewLifecycleOwner) { success ->
            if (success == true && pendingCheckoutAfterLogin) {
                pendingCheckoutAfterLogin = false
                savedStateHandle["login_success"] = false
                // Tự động tiếp tục checkout
                doCheckout()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    adapter.submitList(state.items)
                    tvTotal.text = "Tổng: ${state.total}đ"

                    // Show/hide empty state
                    tvEmpty.visibility = if (state.items.isEmpty()) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (state.items.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        // STEP 4-5: Checkout
        btnCheckout.setOnClickListener {
            // Nếu guest -> chuyển login và nhớ đang muốn checkout
            if (!vm.isLoggedIn()) {
                pendingCheckoutAfterLogin = true
                val args = bundleOf(
                    "redirect" to "cart",
                    "autoCheckout" to true
                )
                findNavController().navigate(R.id.action_cart_to_login, args)
                return@setOnClickListener
            }
            doCheckout()
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun doCheckout() {
        vm.checkout(
            onRequireLogin = {
                pendingCheckoutAfterLogin = true
                val args = bundleOf(
                    "redirect" to "cart",
                    "autoCheckout" to true
                )
                findNavController().navigate(R.id.action_cart_to_login, args)
            },
            onOrderCreated = { orderId ->
                // Lưu orderId hiện tại để có thể theo dõi sau
                OrderStore.saveCurrentOrderId(requireContext(), orderId)

                val bundle = Bundle().apply { putString("orderId", orderId) }
                findNavController().navigate(R.id.action_cart_to_orderTracking, bundle)
            },
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}


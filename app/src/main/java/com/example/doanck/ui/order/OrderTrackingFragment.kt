package com.example.doanck.ui.order

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.doanck.R
import com.example.doanck.data.local.OrderStore
import com.example.doanck.data.repository.OrderRepository
import com.example.doanck.di.ServiceLocator
import kotlinx.coroutines.launch

class OrderTrackingFragment : Fragment(R.layout.fragment_order_tracking) {

    private val vm: OrderTrackingViewModel by viewModels {
        val orderRepo = OrderRepository(ServiceLocator.orderApi, requireContext())
        OrderTrackingViewModel.Factory(orderRepo)
    }

    private lateinit var tvOrderId: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvError: TextView
    private lateinit var progress: ProgressBar
    private lateinit var btnBack: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvOrderId = view.findViewById(R.id.tvOrderId)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvTotal = view.findViewById(R.id.tvTotal)
        tvError = view.findViewById(R.id.tvError)
        progress = view.findViewById(R.id.progress)
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            // Out ra để đặt đơn khác
            findNavController().navigateUp()
        }

        // Ưu tiên lấy orderId từ arguments (khi checkout xong chuyển qua)
        val argOrderId = arguments?.getString("orderId")

        // Nếu click "Theo dõi đơn hàng" từ Home mà không truyền id -> lấy "đơn hiện tại" đã lưu
        val orderId = argOrderId ?: OrderStore.currentOrderId(requireContext())

        if (orderId.isNullOrBlank()) {
            showErrorOnly("Chưa có đơn hàng nào để theo dõi")
            return
        }

        vm.load(orderId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collect { state ->
                    progress.visibility = if (state.loading) View.VISIBLE else View.GONE

                    if (state.error != null) {
                        tvError.visibility = View.VISIBLE
                        tvError.text = state.error
                    } else {
                        tvError.visibility = View.GONE
                    }

                    if (state.orderId.isNotBlank()) {
                        tvOrderId.text = "Mã đơn: ${state.orderId}"
                        tvTotal.text = "Tổng: ${formatVnd(state.total)}"
                        bindStatus(state.status)
                    }
                }
            }
        }
    }

    private fun showErrorOnly(msg: String) {
        progress.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = msg
        tvOrderId.text = "Mã đơn: -"
        tvStatus.text = "Trạng thái: -"
        tvTotal.text = "Tổng: -"
    }

    private fun bindStatus(status: String) {
        val (text, colorRes) = when (status.lowercase()) {
            "pending" -> "Trạng thái: Đang chờ xác nhận" to R.color.orange_accent
            "confirmed" -> "Trạng thái: Đã xác nhận" to R.color.orange_primary
            "preparing" -> "Trạng thái: Đang chuẩn bị" to android.R.color.holo_green_dark
            "delivering" -> "Trạng thái: Đang giao" to android.R.color.holo_blue_dark
            "completed" -> "Trạng thái: Hoàn thành" to android.R.color.holo_green_dark
            "cancelled" -> "Trạng thái: Đã hủy" to android.R.color.holo_red_dark
            else -> "Trạng thái: $status" to R.color.text_primary
        }
        tvStatus.text = text
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    private fun formatVnd(v: Long): String {
        val s = "%,d".format(v).replace(',', '.')
        return "${s}đ"
    }
}


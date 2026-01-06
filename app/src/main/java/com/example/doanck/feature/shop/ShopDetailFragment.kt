package com.example.doanck.feature.shop

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShopDetailFragment : Fragment(R.layout.fragment_shop_detail) {

    private val vm: ShopViewModel by viewModels()

    // ✅ UUID là String, không dùng -1 nữa
    private val shopId: String? by lazy { arguments?.getString("shopId") }
    private val shopName: String by lazy { arguments?.getString("shopName").orEmpty() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = shopId
        if (id.isNullOrBlank()) {
            findNavController().popBackStack()
            return
        }

        val tvShopName = view.findViewById<TextView>(R.id.tvShopName)
        val rv = view.findViewById<RecyclerView>(R.id.rvProducts)

        tvShopName.text = shopName

        val adapter = ProductAdapter()
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.products.collectLatest { adapter.submitList(it) }
        }

        vm.loadProducts(id)
    }
}

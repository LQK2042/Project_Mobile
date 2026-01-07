package com.example.doanck.feature.shop

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.doanck.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShopDetailFragment : Fragment(R.layout.fragment_shop_detail) {

    private val vm: ShopViewModel by viewModels()

    // Arguments từ navigation
    private val shopId: String? by lazy { arguments?.getString("shopId") }
    private val shopName: String by lazy { arguments?.getString("shopName").orEmpty() }
    private val shopAddress: String by lazy { arguments?.getString("shopAddress").orEmpty() }
    private val shopImageUrl: String by lazy { arguments?.getString("shopImageUrl").orEmpty() }

    private lateinit var productAdapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = shopId
        if (id.isNullOrBlank()) {
            findNavController().popBackStack()
            return
        }

        // Bind views
        val imgShop = view.findViewById<ImageView>(R.id.imgShop)
        val tvShopName = view.findViewById<TextView>(R.id.tvShopName)
        val tvShopAddress = view.findViewById<TextView>(R.id.tvShopAddress)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnCart = view.findViewById<ImageButton>(R.id.btnCart)
        val rv = view.findViewById<RecyclerView>(R.id.rvProducts)

        // Hiển thị thông tin shop
        tvShopName.text = shopName
        tvShopAddress.text = if (shopAddress.isNotBlank()) shopAddress else "Địa chỉ quán"

        // Load ảnh shop (nếu có)
        if (shopImageUrl.isNotBlank()) {
            imgShop.load(shopImageUrl) {
                placeholder(R.drawable.ic_avatar_placeholder)
                error(R.drawable.ic_avatar_placeholder)
                crossfade(true)
            }
        } else {
            imgShop.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        // Nút back
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Nút cart
        btnCart.setOnClickListener {
            findNavController().navigate(R.id.action_shopDetail_to_cart)
        }

        // Setup RecyclerView products với callbacks thêm/bớt giỏ hàng
        productAdapter = ProductAdapter(
            onPlus = { product -> vm.onPlus(product) },
            onMinus = { product -> vm.onMinus(product) }
        )
        rv.adapter = productAdapter

        // Observe products
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.products.collectLatest { products ->
                    productAdapter.submitList(products)
                }
            }
        }

        // Observe cart quantities để cập nhật số lượng trên UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.cartQuantities.collectLatest { quantities ->
                    productAdapter.updateQuantities(quantities)
                }
            }
        }

        vm.loadProducts(id)
    }
}

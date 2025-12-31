package com.example.doanck.feature.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val vm: HomeViewModel by viewModels()
    private lateinit var adapter: ShopAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1️⃣ RecyclerView
        val rv = view.findViewById<RecyclerView>(R.id.rvShops)

        adapter = ShopAdapter { shop ->
            val args = bundleOf(
                "shopId" to shop.id,
                "shopName" to shop.name
            )
            findNavController().navigate(
                R.id.action_home_to_shopDetail,
                args
            )
        }

        rv.adapter = adapter

        // 2️⃣ OBSERVE DATA  <<< ĐOẠN BẠN HỎI Ở ĐÂY
        lifecycleScope.launch {
            vm.shops.collect { shops ->
                adapter.submitList(shops)
            }
        }

        // 3️⃣ Load data
        vm.load()
    }
}

package com.example.doanck.feature.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
import com.example.doanck.domain.model.Shop

class ShopAdapter(
    private val onClick: (Shop) -> Unit
) : ListAdapter<Shop, ShopAdapter.ShopVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ShopVH(view, onClick)
    }

    override fun onBindViewHolder(holder: ShopVH, position: Int) {
        holder.bind(getItem(position))
    }

    class ShopVH(
        itemView: View,
        private val onClick: (Shop) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName = itemView.findViewById<TextView>(R.id.tvShopName)
        private val tvAddress = itemView.findViewById<TextView>(R.id.tvShopAddress)

        fun bind(shop: Shop) {
            tvName.text = shop.name
            tvAddress.text = shop.address

            itemView.setOnClickListener {
                onClick(shop)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Shop>() {
            override fun areItemsTheSame(old: Shop, new: Shop): Boolean {
                return old.id == new.id
            }

            override fun areContentsTheSame(old: Shop, new: Shop): Boolean {
                return old == new
            }
        }
    }
}

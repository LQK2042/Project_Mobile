package com.example.doanck.feature.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        private val imgShop = itemView.findViewById<ImageView>(R.id.imgShop)

        fun bind(shop: Shop) {
            tvName.text = shop.name

            Glide.with(itemView)
                .load(shop.logoUrl)
                .centerCrop()
                .into(imgShop)

            itemView.setOnClickListener { onClick(shop) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Shop>() {
            override fun areItemsTheSame(oldItem: Shop, newItem: Shop) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Shop, newItem: Shop) = oldItem == newItem
        }
    }
}

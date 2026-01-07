package com.example.doanck.feature.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.doanck.R

class TopShopAdapter(
    private val onClick: (ShopUi) -> Unit
) : ListAdapter<ShopUi, TopShopAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_top_shop, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(itemView: View, private val onClick: (ShopUi) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val img = itemView.findViewById<ImageView>(R.id.imgShop)
        private val tvName = itemView.findViewById<TextView>(R.id.tvShopName)
        private val tvArea = itemView.findViewById<TextView>(R.id.tvShopArea)

        fun bind(item: ShopUi) {
            img.load(item.logoUrl)
            tvName.text = item.name
            tvArea.text = item.area
            itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ShopUi>() {
            override fun areItemsTheSame(oldItem: ShopUi, newItem: ShopUi): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ShopUi, newItem: ShopUi): Boolean = oldItem == newItem
        }
    }
}

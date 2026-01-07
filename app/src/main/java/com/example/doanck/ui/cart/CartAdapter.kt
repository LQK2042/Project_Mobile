package com.example.doanck.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doanck.R
import com.example.doanck.data.local.CartItemEntity

class CartAdapter(
    private val onPlus: (CartItemEntity) -> Unit,
    private val onMinus: (CartItemEntity) -> Unit
) : ListAdapter<CartItemEntity, CartAdapter.VH>(CartDiffCallback()) {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvQty: TextView = itemView.findViewById(R.id.tvQty)
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.tvName.text = item.name
        holder.tvQty.text = item.quantity.toString()
        holder.tvPrice.text = "${item.price * item.quantity}đ"

        item.imageUrl?.let { url ->
            if (url.isNotBlank()) {
                Glide.with(holder.itemView.context)
                    .load(url)
                    .into(holder.ivProduct)
            }
        }

        holder.btnPlus.setOnClickListener { onPlus(item) }
        holder.btnMinus.setOnClickListener { onMinus(item) }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItemEntity>() {
        override fun areItemsTheSame(oldItem: CartItemEntity, newItem: CartItemEntity): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItemEntity, newItem: CartItemEntity): Boolean {
            return oldItem == newItem
        }
    }
}


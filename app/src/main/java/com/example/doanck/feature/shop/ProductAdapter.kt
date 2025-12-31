package com.example.doanck.feature.shop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
import com.example.doanck.domain.model.Product

class ProductAdapter :
    ListAdapter<Product, ProductAdapter.ProductVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductVH(view)
    }

    override fun onBindViewHolder(holder: ProductVH, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img = itemView.findViewById<ImageView>(R.id.imgProduct)
        private val name = itemView.findViewById<TextView>(R.id.tvProductName)
        private val price = itemView.findViewById<TextView>(R.id.tvProductPrice)

        fun bind(p: Product) {
            name.text = p.name
            price.text = "${p.price} đ"

            // nếu muốn load ảnh sau:
            // Coil.load(img.context, p.imageUrl)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
            override fun areContentsTheSame(old: Product, new: Product) = old == new
        }
    }
}

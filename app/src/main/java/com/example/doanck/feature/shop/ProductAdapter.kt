package com.example.doanck.feature.shop

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
import com.example.doanck.domain.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter : ListAdapter<Product, ProductAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
            override fun areContentsTheSame(old: Product, new: Product) = old == new
        }

        private val vnd = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgProduct)
        val name: TextView = itemView.findViewById(R.id.tvName)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.name.text = item.name
        holder.price.text = vnd.format(item.price)

        val url = item.imageUrl

        if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
            Glide.with(holder.itemView)
                .load(url)
                .centerCrop()
                .into(holder.img)
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }
}

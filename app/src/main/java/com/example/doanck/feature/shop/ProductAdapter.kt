package com.example.doanck.feature.shop

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
import com.example.doanck.domain.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val onPlus: (Product) -> Unit = {},
    private val onMinus: (Product) -> Unit = {}
) : ListAdapter<Product, ProductAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
            override fun areContentsTheSame(old: Product, new: Product) = old == new
        }

        private val vnd = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    }

    // Map lưu số lượng trong giỏ cho mỗi product
    private val quantities = mutableMapOf<String, Int>()

    fun updateQuantities(cartItems: Map<String, Int>) {
        quantities.clear()
        quantities.putAll(cartItems)
        notifyDataSetChanged()
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgProduct)
        val name: TextView = itemView.findViewById(R.id.tvName)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.name.text = item.name
        holder.price.text = vnd.format(item.price)

        // Hiển thị số lượng trong giỏ
        val qty = quantities[item.id] ?: 0
        holder.tvQuantity.text = qty.toString()

        // Ẩn/hiện nút minus và quantity dựa vào số lượng
        holder.btnMinus.visibility = if (qty > 0) View.VISIBLE else View.INVISIBLE
        holder.tvQuantity.visibility = if (qty > 0) View.VISIBLE else View.INVISIBLE

        // Click handlers
        holder.btnPlus.setOnClickListener { onPlus(item) }
        holder.btnMinus.setOnClickListener { onMinus(item) }

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

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
import com.example.doanck.domain.model.ProductSuggest
import java.text.NumberFormat
import java.util.Locale

class ProductSuggestAdapter(
    private val onClick: (ProductSuggest) -> Unit
) : ListAdapter<ProductSuggest, ProductSuggestAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductSuggest>() {
            override fun areItemsTheSame(o: ProductSuggest, n: ProductSuggest) = o.productId == n.productId
            override fun areContentsTheSame(o: ProductSuggest, n: ProductSuggest) = o == n
        }
        private val vnd = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgFood)
        val food: TextView = v.findViewById(R.id.tvFoodName)
        val shop: TextView = v.findViewById(R.id.tvShopName)
        val price: TextView = v.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product_suggest, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.food.text = item.name
        holder.shop.text = item.shopName
        holder.price.text = vnd.format(item.price)

        Glide.with(holder.itemView)
            .load(item.imageUrl)
            .centerCrop()
            .into(holder.img)

        holder.itemView.setOnClickListener { onClick(item) }
    }
}

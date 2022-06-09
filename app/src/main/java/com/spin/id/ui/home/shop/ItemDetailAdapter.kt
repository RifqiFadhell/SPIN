package com.spin.id.ui.home.shop

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.shop.ProductMaster
import com.spin.id.utils.extensions.rupiahFormat
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_detail_commerce.view.*

class ItemDetailAdapter(
    private val context: Context,
    private val itemList: List<ProductMaster>,
    private val onItemClick: ((Int, ProductMaster) -> Unit)? = null
) :
    RecyclerView.Adapter<ItemDetailAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_detail_commerce, parent, false)
        return DefaultViewHolder(view)
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        holder.bindItem(position, itemList[position], onItemClick)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class DefaultViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        @SuppressLint("SetTextI18n")
        fun bindItem(
            itemPosition: Int,
            item: ProductMaster,
            onItemClick: ((Int, ProductMaster) -> Unit)? = null
        ) {
            itemView.textItemProduct.text = item.product_name
            itemView.textPriceProduct.text = item.product_price?.rupiahFormat() ?: "Rp 0"
            containerView.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
        }
    }
}
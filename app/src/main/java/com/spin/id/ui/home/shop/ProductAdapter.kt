package com.spin.id.ui.home.shop

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.shop.DataProduct
import com.spin.id.utils.extensions.loadImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_product_commerce.view.*


class ProductAdapter(
    private val context: Context,
    private val itemList: List<DataProduct>,
    private val onItemClick: ((Int, DataProduct) -> Unit)? = null
) :
    RecyclerView.Adapter<ProductAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_product_commerce, parent, false)
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
            item: DataProduct,
            onItemClick: ((Int, DataProduct) -> Unit)? = null
        ) {
            itemView.textProduct.text = item.subcategoryName
            itemView.imageProduct.loadImage(
                context,
                item.subcategoryImg,
                R.drawable.ic_place_holder_big
            )
            containerView.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
        }
    }
}

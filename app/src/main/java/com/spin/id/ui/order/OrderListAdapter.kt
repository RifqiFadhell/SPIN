package com.spin.id.ui.order

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.order.orderlist.OrderData
import com.spin.id.utils.extensions.convertDate
import com.spin.id.utils.extensions.loadImage
import com.spin.id.utils.extensions.rupiahFormat
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_order.view.*


class OrderListAdapter(
    private val context: Context,
    private val itemList: List<OrderData>,
    private val onItemClick: ((Int, OrderData) -> Unit)? = null
) :
    RecyclerView.Adapter<OrderListAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_order, parent, false)
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
            item: OrderData,
            onItemClick: ((Int, OrderData) -> Unit)? = null
        ) {
            itemView.imageItemOrder.loadImage(
                context,
                item.product.currency_img,
                R.drawable.ic_place_holder
            )
            itemView.textItemOrder?.text = item.product.name
            itemView.textNameProductOrder?.text = item.product.subcategory
            itemView.textIdOrder?.text = item.order_id
            itemView.textTimeOrder?.text =
                item.created_at?.convertDate("yyyy-MM-dd'T'HH:mm:ss", "dd MMMM yyyy - HH:mm")
            itemView.textPriceOrder?.text = item.paid?.toInt()?.rupiahFormat()
            containerView.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
        }
    }
}

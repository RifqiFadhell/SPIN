package com.spin.id.ui.home.shop.detail.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.shop.payment.PaymentItem
import com.spin.id.utils.extensions.loadImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.payment_item.view.*
import kotlinx.android.synthetic.main.payment_item.view.layoutItem
import kotlinx.android.synthetic.main.title_payment_item.view.*

class PaymentMethodAdapter(
    private var context: Context,
    private var itemList: List<PaymentItem>,
    private var onItemClick: ((Int, PaymentItem) -> Unit)? = null
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<*> {

        return when (viewType) {
            TYPE_TITLE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.title_payment_item, parent, false)
                TitleViewHolder(view)
            }

            TYPE_ITEM -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.payment_item, parent, false)
                MainViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val data = itemList[position]
        when (holder) {
            is TitleViewHolder -> holder.bind(data, position)
            is MainViewHolder -> holder.bind(data, position)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position].code) {
            0 -> TYPE_TITLE
            1 -> TYPE_ITEM
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }
    }

    inner class MainViewHolder(override val containerView: View) :
        BaseViewHolder<PaymentItem>(containerView), LayoutContainer {

        @SuppressLint("SetTextI18n")
        override fun bind(item: PaymentItem, position: Int) {
            itemView.imagePayment.loadImage(context, item.logo, R.drawable.ic_place_holder)
            itemView.textPayment.text = item.name
            itemView.layoutItem.setOnClickListener {
                onItemClick?.invoke(position, item)
            }
        }
    }

    inner class TitleViewHolder(override val containerView: View) :
        BaseViewHolder<PaymentItem>(containerView), LayoutContainer {

        @SuppressLint("SetTextI18n")
        override fun bind(item: PaymentItem, position: Int) {
            itemView.textTitlePayment.text = item.name
        }
    }
}

abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T, position: Int)
}
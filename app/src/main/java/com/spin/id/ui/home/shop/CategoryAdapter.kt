package com.spin.id.ui.home.shop

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.shop.Category
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_category_commerce.view.*


class CategoryAdapter(
    private val context: Context,
    private val itemList: List<Category>,
    private val onItemClick: ((Int, Category) -> Unit)? = null
) :
    RecyclerView.Adapter<CategoryAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_category_commerce, parent, false)
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
            item: Category,
            onItemClick: ((Int, Category) -> Unit)? = null
        ) {
            itemView.textCategory.text = item.category_name
            if (item.isChoosen) {
                itemView.textCategory.setBackgroundResource(R.drawable.category_active_background)
                itemView.textCategory.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.text_section_active
                    )
                )
            } else {
                itemView.textCategory.setBackgroundResource(R.drawable.category_inactive_background)
                itemView.textCategory.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.text_section_inactive
                    )
                )
            }
            containerView.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
        }
    }
}

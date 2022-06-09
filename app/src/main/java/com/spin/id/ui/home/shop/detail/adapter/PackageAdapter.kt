package com.spin.id.ui.home.shop.detail.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.shop.detail.JsonMemberPackageItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.package_item.view.*

class PackageAdapter(
    private var context: Context,
    private var itemList: List<JsonMemberPackageItem>,
    private var onItemClick: ((Int, JsonMemberPackageItem) -> Unit)? = null
) :
    RecyclerView.Adapter<PackageAdapter.DefaultViewHolder>() {

    private var selectedItem: Int = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.package_item, parent, false)
        return DefaultViewHolder(view)
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        holder.bindItem(position, itemList[position], onItemClick)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun selectTaskListItem(pos: Int) {
        val previousItem = selectedItem
        selectedItem = pos
        notifyItemChanged(previousItem)
        notifyItemChanged(pos)
    }

    inner class DefaultViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        @SuppressLint("SetTextI18n")
        fun bindItem(
            itemPosition: Int,
            item: JsonMemberPackageItem,
            onItemClick: ((Int, JsonMemberPackageItem) -> Unit)? = null
        ) {
            if (selectedItem == itemPosition) {
                itemView.buttonPackage.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.zumthor
                    )
                )
                itemView.buttonPackage.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    )
                )
            } else {
                itemView.buttonPackage.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.alababster
                    )
                )
                itemView.buttonPackage.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grayTitle
                    )
                )
            }

            itemView.buttonPackage.text = item.packageName
            itemView.buttonPackage.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
                selectTaskListItem(itemPosition)
            }
        }
    }
}
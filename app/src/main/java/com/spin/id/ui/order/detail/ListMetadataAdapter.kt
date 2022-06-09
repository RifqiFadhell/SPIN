package com.spin.id.ui.order.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.order.orderdetail.Detail
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_metadata.*


class ListMetadataAdapter(
    private val context: Context,
    private val itemList: List<Detail>
) :
    RecyclerView.Adapter<ListMetadataAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_metadata, parent, false)
        return DefaultViewHolder(view)
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        holder.bindItem(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class DefaultViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem(
            item: Detail
        ) {
            titleItemOrder?.text = item.field_name
            textItemOrder?.text = item.value
        }
    }
}

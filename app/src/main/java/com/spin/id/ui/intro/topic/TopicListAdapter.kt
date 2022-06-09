package com.spin.id.ui.intro.topic

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.topic.TopicData
import com.spin.id.utils.extensions.loadImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_topic.*

class TopicListAdapter(
    private val context: Context,
    private val itemList: List<TopicData>,
    private val onItemClick: ((Int, TopicData) -> Unit)? = null
) :
    RecyclerView.Adapter<TopicListAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_topic, parent, false)
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

        fun bindItem(
            itemPosition: Int,
            item: TopicData,
            onItemClick: ((Int, TopicData) -> Unit)? = null
        ) {
            imageItemTopic?.loadImage(context, item.image, R.drawable.ic_plus_btn)
            textItemTopic?.text = item.name
            textDescriptionTopic?.text = item.description
            if (item.isSelected) {
                buttonCheck?.setImageResource(R.drawable.ic_check_btn_dark_blue)
                textItemTopic?.setTypeface(null, Typeface.BOLD)
            } else {
                buttonCheck?.setImageResource(R.drawable.ic_plus_btn)
                textItemTopic?.setTypeface(null, Typeface.NORMAL)
            }
            containerView.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
        }
    }
}
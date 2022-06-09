package com.spin.id.ui.intro.game

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.game.GameData
import com.spin.id.utils.extensions.loadImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_game.*

class GameListAdapter(
    private val context: Context,
    private val itemList: List<GameData>,
    private val onItemClick: ((Int, GameData) -> Unit)? = null
) :
    RecyclerView.Adapter<GameListAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_game, parent, false)
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
            item: GameData,
            onItemClick: ((Int, GameData) -> Unit)? = null
        ) {
            imageItemGame?.loadImage(context, item.image, R.drawable.ic_plus_btn)
            textItemGame?.text = item.name
            if (item.isSelected) {
                buttonCheck?.setImageResource(R.drawable.ic_check_btn_dark_blue)
                textItemGame?.setTypeface(null, Typeface.BOLD)
            } else {
                buttonCheck?.setImageResource(R.drawable.ic_plus_btn)
                textItemGame?.setTypeface(null, Typeface.NORMAL)
            }
            containerView.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
        }
    }
}
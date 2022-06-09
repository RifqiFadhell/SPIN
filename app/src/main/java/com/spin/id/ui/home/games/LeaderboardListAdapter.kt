package com.spin.id.ui.home.games

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.mission.leaderboard.Rank
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_board.view.*

class LeaderboardListAdapter(
    private val context: Context,
    private val itemList: List<Rank>,
    private val username: String? = ""
) :
    RecyclerView.Adapter<LeaderboardListAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_board, parent, false)
        return DefaultViewHolder(view)
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        holder.bindItem(position, itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class DefaultViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        @SuppressLint("SetTextI18n")
        fun bindItem(
            itemPosition: Int,
            item: Rank
        ) {
            itemView.textRankLeaderboard.text = item.rank.toString()
            itemView.textUsernameLeaderboard.text = item.username
            itemView.textTokenLeaderboard.text = item.token.toString()
            if (item.username.equals(username)) {
                containerView.setBackgroundColor(Color.parseColor("#503EC100"))
            } else {
                containerView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
        }
    }
}
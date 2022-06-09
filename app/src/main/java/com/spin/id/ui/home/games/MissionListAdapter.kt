package com.spin.id.ui.home.games

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.mission.mission.Task
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_list_mission.view.*

class MissionListAdapter(
    private val context: Context,
    private val itemList: List<Task>,
    private val onItemClick: ((Int, Task) -> Unit)? = null
) :
    RecyclerView.Adapter<MissionListAdapter.DefaultViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_mission, parent, false)
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
            item: Task,
            onItemClick: ((Int, Task) -> Unit)? = null
        ) {
            itemView.textTitleMission.text = item.task_name
            itemView.textTargetToken.text = "${item.task_token} token"
            itemView.progressMission.max = item.task_limit
            itemView.progressMission.progress = item.user_task_completion
            //JIKA TASK YANG COMPLETE == TASK LIMIT
            if (item.user_task_completion.equals(item.task_limit)) {
                itemView.textProgressMission.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_check_btn_sea_blue,
                    0,
                    0,
                    0
                )
                itemView.textProgressMission.text = ""
            } else {
                itemView.textProgressMission.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                itemView.textProgressMission.text =
                    "${item.user_task_completion}/${item.task_limit}"
            }
            containerView.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
        }
    }
}
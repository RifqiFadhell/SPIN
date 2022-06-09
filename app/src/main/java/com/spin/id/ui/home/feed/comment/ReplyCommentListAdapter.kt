package com.spin.id.ui.home.feed.comment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.feed.comment.ChildCommentData
import com.spin.id.utils.TimeUtils
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.sub_comment_item.*

class ReplyCommentListAdapter(
    private val context: Context,
    private val userId: String,
    private val isAdmin: Boolean,
    private val itemList: List<ChildCommentData>,
    private val onLikeClick: ((Int, ChildCommentData) -> Unit)? = null,
    private val onReplyClick: ((Int, ChildCommentData) -> Unit)? = null,
    private val onOptionClick: ((Int, ChildCommentData) -> Unit)? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var totalSize = 0

    companion object {
        const val LOADING_VIEW = 1
        const val ITEM_VIEW = 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.sub_comment_item, parent, false)
                DefaultViewHolder(view)
            }
            LOADING_VIEW -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.footer_loading_view, parent, false)
                return LoadingViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.sub_comment_item, parent, false)
                DefaultViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DefaultViewHolder -> {
                holder.bindItem(
                    position,
                    itemList[position],
                    onLikeClick,
                    onReplyClick,
                    onOptionClick
                )
            }
            is LoadingViewHolder -> {
                holder.bindItem()
            }

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int =
        if (position == itemList.size - 1 && itemList.size != totalSize) {
            LOADING_VIEW
        } else {
            ITEM_VIEW
        }


    inner class DefaultViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem(
            itemPosition: Int,
            item: ChildCommentData,
            onLikeClick: ((Int, ChildCommentData) -> Unit)? = null,
            onReplyClick: ((Int, ChildCommentData) -> Unit)? = null,
            onOptionClick: ((Int, ChildCommentData) -> Unit)? = null
        ) {
            textName?.text = item.username
            if (item.role.equals("admin", true)) {
                textAdmin?.toVisible()
            } else {
                textAdmin?.toGone()
            }
            if (item.user_id.equals(userId) || isAdmin) {
                buttonOptionComment?.toVisible()
            } else {
                buttonOptionComment?.toGone()
            }
            textCaption?.text = item.comment
            buttonLike?.text = item.likes_count
            if (item.like_status.equals("ACTIVE")) {
                buttonLike?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0)
            } else {
                buttonLike?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unlike, 0, 0, 0)
            }
            textTime?.text = TimeUtils.setTimeAgo(context, item.created_at)
            buttonLike?.setOnClickListener {
                onLikeClick?.invoke(itemPosition, item)
            }
            buttonReply?.setOnClickListener {
                onReplyClick?.invoke(itemPosition, item)
            }
            buttonOptionComment?.setOnClickListener {
                onOptionClick?.invoke(itemPosition, item)
            }
        }
    }

    inner class LoadingViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem() {

        }
    }
}
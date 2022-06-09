package com.spin.id.ui.home.feed.comment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.R
import com.spin.id.api.response.feed.comment.CommentData
import com.spin.id.utils.TimeUtils
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.comment_item.*

class CommentListAdapter(
    private val context: Context,
    private val userId: String,
    private val positionPos: Int,
    private val isAdmin: Boolean,
    private val itemList: List<CommentData>,
    private val onLikeClick: ((Int, Int, CommentData) -> Unit)? = null,
    private val onReplyClick: ((Int, Int, CommentData) -> Unit)? = null,
    private val onSeeReplyClick: ((Int, Int, CommentData) -> Unit)? = null,
    private val onOptionClick: ((Int, Int, CommentData) -> Unit)? = null
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
                    .inflate(R.layout.comment_item, parent, false)
                DefaultViewHolder(view)
            }
            LOADING_VIEW -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.footer_loading_view, parent, false)
                return LoadingViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.comment_item, parent, false)
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
                    onSeeReplyClick,
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
            item: CommentData,
            onLikeClick: ((Int, Int, CommentData) -> Unit)? = null,
            onReplyClick: ((Int, Int, CommentData) -> Unit)? = null,
            onSeeReplyClick: ((Int, Int, CommentData) -> Unit)? = null,
            onOptionClick: ((Int, Int, CommentData) -> Unit)? = null
        ) {
            textName?.text = item.username
            if (item.role.equals("admin", true)) {
                textAdmin?.toVisible()
            } else {
                textAdmin?.toGone()
            }
            textCaption?.text = item.comment
            buttonLike?.text = item.likes_count
            textTime?.text = TimeUtils.setTimeAgo(context, item.created_at)
            buttonLike?.setOnClickListener {
                onLikeClick?.invoke(itemPosition, positionPos, item)
            }
            if (item.like_status.equals("ACTIVE", true)) {
                buttonLike?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0)
            } else {
                buttonLike?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unlike, 0, 0, 0)
            }
            buttonReply?.setOnClickListener {
                onReplyClick?.invoke(itemPosition, positionPos, item)
            }
            if (item.user_id.equals(userId) || isAdmin) {
                buttonOptionComment?.toVisible()
            } else {
                buttonOptionComment?.toGone()
            }
            if (!item.comments_count.isNullOrEmpty() && !item.comments_count.equals("0")) {
                buttonSeeReply?.toVisible()
                buttonSeeReply?.text = context.getString(R.string.see_reply_label, item.comments_count)
            } else {
                buttonSeeReply?.toGone()
            }
            buttonSeeReply?.setOnClickListener {
                onSeeReplyClick?.invoke(itemPosition, positionPos, item)
            }
            buttonOptionComment?.setOnClickListener {
                onOptionClick?.invoke(itemPosition, positionPos, item)
            }
        }
    }

    inner class LoadingViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem() {

        }
    }
}
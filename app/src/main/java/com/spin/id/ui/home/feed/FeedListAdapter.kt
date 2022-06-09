package com.spin.id.ui.home.feed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.amplitude.api.Amplitude
import com.spin.id.R
import com.spin.id.api.response.feed.list.FeedData
import com.spin.id.utils.TimeUtils
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_POST_VIEWED
import com.spin.id.utils.analytic.VisibilityTracker
import com.spin.id.utils.extensions.loadImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.feed_item.*
import kotlinx.android.synthetic.main.feed_item.textTitle
import kotlinx.android.synthetic.main.last_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class FeedListAdapter(
    private val context: Context,
    private var itemList: List<FeedData>,
    private val onItemClick: ((Int, FeedData) -> Unit)? = null,
    private val onOptionClick: ((Int, FeedData) -> Unit)? = null,
    private val onLikeClick: ((Int, FeedData) -> Unit)? = null,
    private val onCommentClick: ((Int, FeedData) -> Unit)? = null,
    private val onShareClick: ((Int, FeedData) -> Unit)? = null,
    private val onBackTop: ((Int, FeedData) -> Unit)? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //var totalSize = 0

    companion object {
        const val LOADING_VIEW = 1
        const val ITEM_VIEW = 0
        const val FOOTER_VIEW = 2
    }

    private var mVisibilityTracker: VisibilityTracker? = null
    private val mViewPositionMap = WeakHashMap<View, Int>()

    fun impressionAdapter(
        activity: Activity,
        dataSet: List<FeedData>
    ) {
        itemList = dataSet
        mVisibilityTracker = VisibilityTracker(activity)
        mVisibilityTracker?.setVisibilityTrackerListener(object :
            VisibilityTracker.VisibilityTrackerListener {
            override fun onVisibilityChanged(
                visibleViews: List<View>?,
                invisibleViews: List<View>?
            ) {
                if (visibleViews != null) {
                    handleVisibleViews(visibleViews)
                }
            }
        })
    }

    private fun handleVisibleViews(visibleViews: List<View>) {
        for (v in visibleViews) {
            val viewPosition = mViewPositionMap[v]
            val viewTitle: String = itemList[viewPosition!!].platform_name.toString()
//            Log.d("Impression Tracking", viewTitle)
            val eventProperties = JSONObject()
            try {
                eventProperties.put("Post ID", itemList[viewPosition].post_id)
                eventProperties.put("Post Description", itemList[viewPosition].caption)
                eventProperties.put("Post Game Main Category", itemList[viewPosition].game_name)
                eventProperties.put("Post Topic Main Category", itemList[viewPosition].topic_name)
                eventProperties.put("Post Moderator", "admin")
                eventProperties.put("Post Creator", itemList[viewPosition].publisher_name)
                eventProperties.put("Post Platform", itemList[viewPosition].platform_name)
                eventProperties.put("Post Schedule", itemList[viewPosition].publishing_schedule)
                eventProperties.put("Post Source", "Feed")
            } catch (exception: JSONException) {
            }
            Amplitude.getInstance().logEvent(PARAM_POST_VIEWED, eventProperties)
            val intent = Intent("custom-message")
            intent.putExtra("post_id", itemList[viewPosition].post_id)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.feed_item, parent, false)
                DefaultViewHolder(view)
            }
            LOADING_VIEW -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.footer_loading_view, parent, false)
                return LoadingViewHolder(view)
            }
            FOOTER_VIEW -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.last_layout, parent, false)
                return FooterViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.feed_item, parent, false)
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
                    onItemClick,
                    onOptionClick,
                    onLikeClick,
                    onCommentClick,
                    onShareClick
                )
                mViewPositionMap[holder.itemView] = position
                mVisibilityTracker?.addView(holder.itemView, 0)
            }
            is LoadingViewHolder -> {
                holder.bindItem()
            }
            is FooterViewHolder -> {
                holder.bindItem(position, itemList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position].viewType) {
            0 -> ITEM_VIEW
            1 -> LOADING_VIEW
            2 -> FOOTER_VIEW
            else -> ITEM_VIEW
        }
    }

    inner class LoadingViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem() {

        }
    }

    inner class FooterViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem(itemPosition: Int, item: FeedData) {
            buttonTop?.setOnClickListener {
                onBackTop?.invoke(itemPosition, item)
            }
        }
    }

    inner class DefaultViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem(
            itemPosition: Int,
            item: FeedData,
            onItemClick: ((Int, FeedData) -> Unit)? = null,
            onOptionClick: ((Int, FeedData) -> Unit)? = null,
            onLikeClick: ((Int, FeedData) -> Unit)? = null,
            onCommentClick: ((Int, FeedData) -> Unit)? = null,
            onShareClick: ((Int, FeedData) -> Unit)? = null

        ) {
            imagePublisher?.loadImage(context, item.platform_image, R.drawable.ic_place_holder)
            textTime?.text = TimeUtils.setTimeAgo(context, item.publishing_schedule.toString())
            textPublisher?.text = item.publisher_name
            imageItem?.loadImage(context, item.thumbnail, R.drawable.ic_place_holder_big)
            textTitle?.text = item.caption
            buttonLike?.text = item.likes_count
            buttonComment?.text = item.comments_count
            if (item.like_status.equals("ACTIVE", true)) {
                buttonLike?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0)
            } else {
                buttonLike?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unlike, 0, 0, 0)
            }
            imageItem?.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
            textTitle?.setOnClickListener {
                onItemClick?.invoke(itemPosition, item)
            }
            buttonOption?.setOnClickListener {
                onOptionClick?.invoke(itemPosition, item)
            }
            buttonLike?.setOnClickListener {
                onLikeClick?.invoke(itemPosition, item)
            }
            buttonComment?.setOnClickListener {
                onCommentClick?.invoke(itemPosition, item)
            }
            buttonShare?.setOnClickListener {
                onShareClick?.invoke(itemPosition, item)
            }
        }
    }
}
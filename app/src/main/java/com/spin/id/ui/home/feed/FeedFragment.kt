package com.spin.id.ui.home.feed

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.gson.Gson
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.spin.id.BuildConfig
import com.spin.id.R
import com.spin.id.api.request.feed.*
import com.spin.id.api.request.impression.DataImpression
import com.spin.id.api.request.impression.ImpressionRequest
import com.spin.id.api.request.redeem.RedeemRequest
import com.spin.id.api.request.redeem.Task
import com.spin.id.api.response.banner.BannerData
import com.spin.id.api.response.banner.BannerResponse
import com.spin.id.api.response.feed.comment.CommentData
import com.spin.id.api.response.feed.comment.CommentResponse
import com.spin.id.api.response.feed.detail.DataItem
import com.spin.id.api.response.feed.detail.DetailFeedResponse
import com.spin.id.api.response.feed.like.LikeCommentResponse
import com.spin.id.api.response.feed.like.LikeFeedResponse
import com.spin.id.api.response.feed.list.FeedData
import com.spin.id.api.response.feed.list.FeedResponse
import com.spin.id.api.response.game.GameData
import com.spin.id.api.response.impression.ImpressionResponse
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.mission.redeem.RedeemResponse
import com.spin.id.api.response.topic.TopicData
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DATA_COMMENT_LIKE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DEEP_LINK
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DEEP_LINK_USER_ID
import com.spin.id.preference.tinyDb.TinyConstant.TINY_LIST_IMPRESSION
import com.spin.id.preference.tinyDb.TinyConstant.TINY_LIST_TOPIC
import com.spin.id.preference.tinyDb.TinyConstant.TINY_STATUS_LOGIN
import com.spin.id.preference.tinyDb.TinyConstant.TINY_TOKEN
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.home.HomeActivity
import com.spin.id.ui.home.feed.FeedConstant.DOMAIN_PREFIX_DEEP_LINK
import com.spin.id.ui.home.feed.FeedConstant.URL_DEEP_LINK
import com.spin.id.ui.home.feed.comment.CommentListAdapter
import com.spin.id.ui.home.feed.comment.DeleteCommentDialog
import com.spin.id.ui.home.feed.comment.ReplyCommentListDialog
import com.spin.id.ui.home.games.data.DailyTask
import com.spin.id.ui.intro.IntroActivity
import com.spin.id.ui.order.detail.OrderDetailActivity
import com.spin.id.ui.webview.webview.builder.WebviewBuilder
import com.spin.id.ui.webview.webview.constant.AppSourceConstant
import com.spin.id.ui.webview.webview.constant.HttpMethodConstant
import com.spin.id.utils.LoaderDialogHelper
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.NavigationBarUtil
import com.spin.id.utils.TimeUtils
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.FEED
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_BANNER_CLICKED
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_BANNER_SWIPED
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_DEEP_LINK_LOAD
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_FEED_EMPTY
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_FEED_ERROR
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_FEED_LOAD
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_FEED_LOAD_PAGINATION
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_ON_BOARDING_LOAD
import com.spin.id.utils.base.BaseFragment
import com.spin.id.utils.extensions.*
import com.synnapps.carouselview.ImageListener
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.bottom_sheet_toolbar.*
import kotlinx.android.synthetic.main.comment_fragment.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.feed_fragment.*
import kotlinx.android.synthetic.main.feed_item.*
import kotlinx.android.synthetic.main.feed_item.buttonOption
import kotlinx.android.synthetic.main.shared_feed_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FeedFragment : BaseFragment(), FeedContract.FeedView, FeedContract.BannerView,
    FeedContract.FeedPageView, FeedContract.DetailFeedView, FeedContract.LikeFeedView,
    FeedContract.LikeFeedDetailView,
    FeedContract.CommentListFeedView, FeedContract.CommentListPageView,
    FeedContract.CommentFeedView, FeedContract.LikeCommentView,
    FeedContract.DeleteCommentFeedView, FeedContract.Redeem, FeedContract.Impression {

    private lateinit var presenter: FeedPresenter
    private lateinit var adapter: FeedListAdapter
    private lateinit var adapterComment: CommentListAdapter
    private var itemList = ArrayList<FeedData>()
    private var itemListComment = ArrayList<CommentData>()
    private var pageId = ArrayList<String>()
    private var itemListBanner = ArrayList<BannerData>()
    private var listNameGame = ArrayList<String>()
    private var listNameTopic = ArrayList<String>()
    private var listImpression = ArrayList<DataImpression>()

    private var progress: LoaderIndicatorHelper? = null
    private lateinit var bottomDialog: BottomSheetDialog
    private lateinit var commentDialog: BottomSheetDialog
    private var viewComment: View? = null
    private var views: View? = null

    private var currentPage = 1
    private var currentPageComment = 1
    private var totalPage = 0
    private var totalPageComment = 0

    private var newPost: Int = 0
    private var tinyDb: TinyDB? = null

    private var token: String? = ""
    private var idPost: String? = ""
    private var isLogin: Boolean? = false
    private var statusPreference: Boolean? = false
    private var statusPage: String? = ""
    private var responses: DataItem? = null

    private var tracker: AmplitudeClient? = null
    lateinit var lenearLayoutManager: LinearLayoutManager

    private var totalSizeComment = 0

    override fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.feed_fragment, container, false)
    }

    var messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val postId = intent.getStringExtra("post_id")
            val data = DataImpression("impression", postId)
            listImpression.plusAssign(data)
        }
    }

    override fun getScreenName() = FEED

    override fun init(bundle: Bundle?) {
        tinyDb = TinyDB(requireContext())
        token = tinyDb?.getString(TINY_TOKEN)
        presenter = FeedPresenter()
        progress = LoaderIndicatorHelper()
        isLogin = tinyDb?.getBoolean(TINY_STATUS_LOGIN)
        if (!tinyDb?.getListObject(TINY_LIST_TOPIC, TopicData::class.java)
                .isNullOrEmpty()
        ) statusPreference = true
        tracker = Amplitude.getInstance()
        adapter = FeedListAdapter(
            requireContext(),
            itemList,
            onItemClick = { pos: Int, item: FeedData ->
                goToWebView(
                    "${item.post_url}?post_id=${item.post_id}&utm_source=spin&utm_medium=post&utm_campaign=feed",
                    item.caption.toString(),
                    item.post_url.toString(),
                    item.post_id.toString(),
                    item.likes_count.toString(),
                    item.comments_count.toString(),
                    item
                )
                trackerItemFeed(1, item)
            },
            onOptionClick = { pos: Int, item: FeedData -> validateLogin(2, pos, item) },
            onLikeClick = { pos: Int, item: FeedData -> validateLogin(3, pos, item) },
            onCommentClick = { pos: Int, item: FeedData -> validateLogin(4, pos, item) },
            onShareClick = { pos: Int, item: FeedData -> validateLogin(5, pos, item) },
            onBackTop = { pos: Int, item: FeedData -> scroll?.smoothScrollTo(0, 0) }
        )
        adapter.impressionAdapter(requireActivity(), itemList)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(messageReceiver, IntentFilter("custom-message"))
        setBottomSheet()
        setBottomSheetComment()
        lenearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        listFeed?.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        listFeed.setHasFixedSize(true)
        (listFeed.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        listFeed?.adapter = adapter
    }

    override fun initData(bundle: Bundle?) {
        if (!tinyDb?.getString(TINY_DEEP_LINK).isNullOrEmpty()) validateDeepLink()
        if (!tinyDb?.getString("post_id").isNullOrEmpty()) validateNotification()
        if (!tinyDb?.getString("order_id")
                .isNullOrEmpty()
        ) gotoOrderDetail(tinyDb?.getString("order_id").toString())
        getListFeed()
        showProgress()
        val userId = if (getUserId().isNotEmpty()) getUserId() else {
            if (getSelectedIdGames().isNotEmpty() || getSelectedIdTopic().isNotEmpty()) "0"
            else ""
        }
        presenter.getListBanner(this, userId)
    }

    @SuppressLint("CheckResult")
    override fun initListener(bundle: Bundle?) {
        buttonRefreshError?.setOnClickListener {
            initData(bundle)
        }
        scroll.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY >= v.getChildAt(v.childCount - 1)
                        .measuredHeight - v.measuredHeight &&
                    scrollY > oldScrollY && currentPage < totalPage
                ) {
                    itemList.removeAt(itemList.size - 1)
                    currentPage++
                    getNextFeed(currentPage)
                }
            }
        }

        swipeRefresh.setOnRefreshListener {
            refresh()
        }

        buttonNewPost?.setOnClickListener {
            refresh()
        }

        bottomDialog.imageItem.setOnClickListener {
            responses?.let { it1 -> trackerDetailFeed(1, it1) }
            goToWebView(
                "${responses?.postUrl.toString()}?post_id=${responses?.postId}&utm_source=spin&utm_medium=post&utm_campaign=$statusPage",
                responses?.caption.toString(),
                responses?.postUrl.toString(),
                responses?.postId.toString(),
                responses?.likesCount.toString(),
                responses?.commentsCount.toString(),
                null
            )
        }
        bottomDialog.textTitle.setOnClickListener {
            responses?.let { it1 -> trackerDetailFeed(1, it1) }
            goToWebView(
                "${responses?.postUrl.toString()}?post_id=${responses?.postId}&utm_source=spin&utm_medium=post&utm_campaign=$statusPage",
                responses?.caption.toString(),
                responses?.postUrl.toString(),
                responses?.postId.toString(),
                responses?.likesCount.toString(),
                responses?.commentsCount.toString(), null
            )
        }

        bottomDialog.buttonLike.setOnClickListener { validateLoginDetail(3, responses) }
        bottomDialog.buttonComment.setOnClickListener { validateLoginDetail(4, responses) }
        bottomDialog.buttonOption.setOnClickListener { validateLoginDetail(2, responses) }
        bottomDialog.buttonShare.setOnClickListener { validateLoginDetail(5, responses) }
        commentDialog.buttonBack.setOnClickListener {
            commentDialog.behavior.state = BottomSheetBehavior.STATE_HIDDEN
            commentDialog.behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        commentDialog.editComment.afterTextChangeEvents()
            .skip(1)
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                commentDialog.textCounter.text =
                    getString(R.string.counter_comment, it.editable?.length.toString())
                commentDialog.editComment.filters += InputFilter.LengthFilter(500)
            }

        bottomDialog.buttonDismiss.setOnClickListener {
            bottomDialog.dismiss()
        }
    }

    /*Banner*/

    override fun getData(result: BannerResponse) {
        if (result.status != 200) requireContext().showErrorDialog()
        val data = result.data
        if (!data.isNullOrEmpty()) {
            itemListBanner.clear()
            itemListBanner.addAll(data)
            carouselPromo?.toVisible()
            if (statusPreference == true) {
                itemListBanner.removeAt(0)
                if (itemListBanner.isEmpty()) {
                    carouselPromo?.toGone()
                }
            }
            Log.e("DATA BANNER", Gson().toJson(itemListBanner))
            carouselPromo?.setImageListener(imageListener)
            carouselPromo?.pageCount = itemListBanner.size
            carouselPromo?.setImageClickListener { position ->
                trackerBanner(1, itemListBanner[position])
                if (itemListBanner[position].name.equals("Setting Postingan Anda")) {
                    val b = Bundle()
                    b.putString("type", "set-preference")
                    activity?.goToActivity(IntroActivity::class.java, b)
                } else {
                    val url = itemListBanner[position].url
                    val caption = itemListBanner[position].caption
                    if (!url.isNullOrEmpty()) {

                        val finalUrl: String = if (url.contains("?")) {
                            url + "post_id=${itemListBanner[position].id}&utm_source=spin&utm_medium=post&utm_campaign=promo-banner"
                        } else {
                            "$url?post_id=${itemListBanner[position].id}&utm_source=spin&utm_medium=post&utm_campaign=promo-banner"
                        }

                        goToWebView(
                            finalUrl,
                            caption ?: "Promo",
                            url, "0", "", "", null
                        )
                    }
                }
            }
        } else {
            carouselPromo?.toGone()
        }
    }

    private val imageListener: ImageListener =
        ImageListener { position, imageView ->
            context?.let { ctx ->
                imageView.scaleType = ImageView.ScaleType.FIT_XY
                Glide.with(ctx)
                    .load(itemListBanner[position].thumbnail)
                    .error(R.drawable.ic_place_holder_big)
                    .into(imageView)
            }
        }

    /*List Feed*/

    private fun likeFeed(pos: Int, postId: String) {
        val request = LikeFeedRequest(getUserId(), postId)
        idPost = postId
        presenter.likeFeed(this, pos, request, token.toString())
    }

    private fun getListFeed() {
        val request = FeedRequest()
        //TODO : BENERIN NIH KACAU
        request.user_id = getUserId()
        request.games = getSelectedIdGames()
        request.topic = getSelectedIdTopic()
        request.current_page = currentPage
        presenter.getListFeed(this, request)
    }

    private fun getNextFeed(page: Int) {
        val request = FeedRequest()
        request.user_id = getUserId()
        request.games = getSelectedIdGames()
        request.topic = getSelectedIdTopic()
        request.current_page = page
        request.first_page_post_id = pageId
        presenter.getNextListFeed(this, request)
    }

    private fun refresh() {
        pageId.clear()
        itemList.clear()
        newPost = 0
        currentPage = 1
        totalPage = 0
        //adapter.totalSize = 0
        adapter.notifyDataSetChanged()
        getListFeed()
        swipeRefresh.isRefreshing = false
    }

    override fun getData(result: FeedResponse) {
        hideProgress()
        if (result.status != 200) requireContext().showErrorDialog()
        trackerLoadFeed(1)
        val data = result.data
        newPost = result.new_posts
        showNewPost(newPost)
        if (!data.isNullOrEmpty()) {
            totalPage = result.total_pages
            hideErrorLayout()
            itemList.clear()
            itemList.addAll(data)
            pageId.clear()
            pageId.addAll(result.first_page_post_id)

            if (itemList.size == result.total_items) {
                itemList.add(FeedData(2))
            } else {
                itemList.add(FeedData(1))
            }
            /*adapter.totalSize = totalItems*/
            adapter.notifyDataSetChanged()
        } else {
            trackerLoadFeed(3)
            showErrorLayout()
            textTitleError.text = getString(R.string.error_empty_content_label)
            textTitleError.text = getString(R.string.suberror_empty_label)
        }
    }

    override fun getDataNext(result: FeedResponse) {
        trackerLoadFeed(2)
        val data = result.data
        newPost = result.new_posts
        showNewPost(newPost)
        if (!data.isNullOrEmpty()) {
            hideErrorLayout()
            itemList.addAll(data)
            pageId.clear()
            pageId.addAll(result.first_page_post_id)
            adapter.notifyDataSetChanged()

            Log.e("ITEMLIST SIZE", itemList.size.toString())
            if (itemList.size == result.total_items) {
                itemList.add(FeedData(2))
            } else {
                itemList.add(FeedData(1))
            }
        }
    }

    private fun showNewPost(newPost: Int) {
        if (newPost == 1) {
            buttonNewPost?.toVisible()
        } else {
            buttonNewPost?.toGone()
        }
    }

    override fun getTotalLike(position: Int, result: LikeFeedResponse) {
        if (result.status != 200) requireContext().showErrorDialog()
        val totalLike = result.total_post_likes
        val likeStatus = result.data[0].like_status
        if (result.data[0].like_status == "ACTIVE") {
            redeem(idPost.toString(), "like")
        }
        itemList[position].likes_count = totalLike
        itemList[position].like_status = likeStatus
        adapter.notifyItemChanged(position)
    }

    override fun getTotalLikeComment(
        positionPost: Int,
        positionComment: Int,
        result: LikeCommentResponse
    ) {
        if (result.status != 200) requireContext().showErrorDialog()
        val totalLikeComment = result.total_comment_likes
        val totalLikePost = result.total_post_likes
        val likeStatus = result.data[0].like_status

        itemList[positionPost].likes_count = totalLikePost
        adapter.notifyItemChanged(positionPost)

        itemListComment[positionComment].likes_count = totalLikeComment
        itemListComment[positionComment].like_status = likeStatus
        adapterComment.notifyItemChanged(positionComment)
    }

    override fun showErrorLike(throwable: Throwable) {
        context?.showShortToast(throwable.message.toString())
    }


    /*Shared Feed*/

    @SuppressLint("InflateParams")
    private fun setBottomSheet() {
        bottomDialog = BottomSheetDialog(requireContext())
        views = layoutInflater.inflate(R.layout.shared_feed_layout, null)
        views?.let { bottomDialog.setContentView(it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NavigationBarUtil.setWhiteNavigationBar(requireActivity(), bottomDialog)
        }

        val params = (views?.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        params.height =
            requireActivity().getScreenHeight() - (requireActivity().getActionBarHeight())
        val behavior = params.behavior
        (behavior as BottomSheetBehavior<*>).peekHeight =
            requireActivity().getScreenHeight() - (requireActivity().getActionBarHeight())
        bottomDialog.currentFocus
    }

    private fun setDetailFeed(response: DataItem) {
        responses = response
        bottomDialog.imagePublisher?.loadImage(
            requireContext(),
            response.platformImage,
            R.drawable.ic_place_holder
        )
        bottomDialog.textTime?.text =
            TimeUtils.setTimeAgo(requireContext(), response.publishingSchedule.toString())
        bottomDialog.textPublisher?.text = response.publisherName
        bottomDialog.imageItem?.loadImage(
            requireContext(),
            response.thumbnail,
            R.drawable.ic_place_holder_big
        )
        bottomDialog.textTitle?.text = response.caption
        bottomDialog.buttonLike?.text = response.likesCount
        bottomDialog.buttonComment?.text = response.commentsCount
        bottomDialog.show()

        val eventProperties = JSONObject()
        try {
            eventProperties.put(
                "Sharer User ID",
                tinyDb?.getString(TINY_DEEP_LINK_USER_ID).toString()
            )
        } catch (exception: JSONException) {
        }
        tracker?.logEvent(PARAM_DEEP_LINK_LOAD, eventProperties)
    }

    private fun likeFeed(data: DataItem) {
        val request = LikeFeedRequest(getUserId(), data.postId.toString())
        presenter.likeDetailFeed(this, request, token.toString())
    }

    override fun getTotalLike(result: LikeFeedResponse) {
        bottomDialog.buttonLike?.text = result.total_post_likes
        if (result.data[0].like_status == "ACTIVE") {
            redeem(idPost.toString(), "like")
            bottomDialog.buttonLike.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_like,
                0,
                0,
                0
            )
        } else {
            bottomDialog.buttonLike.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_unlike,
                0,
                0,
                0
            )
        }
        if (result.status != 200) requireContext().showErrorDialog()
    }

    /*Comment*/
    private fun setBottomSheetComment() {
        commentDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        viewComment = layoutInflater.inflate(R.layout.comment_fragment, null)
        viewComment?.let {
            commentDialog.setContentView(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NavigationBarUtil.setWhiteNavigationBar(requireActivity(), commentDialog)
        }

        val params = (viewComment?.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        val behavior = params.behavior
        (behavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
        commentDialog.currentFocus
    }

    //WHEN FIRST LOAD
    override fun getDataComment(result: CommentResponse) {
        val data = result.data
        totalSizeComment = result.total_items
        totalPageComment = result.total_pages
        if (!data.isNullOrEmpty()) {
            commentDialog.emptyComment?.toGone()
            commentDialog.listComment?.toVisible()
            itemListComment.clear()
            itemListComment.addAll(data)
            adapterComment.totalSize = totalSizeComment
            adapterComment.notifyDataSetChanged()
        } else {
            itemListComment.clear()
            commentDialog.emptyComment?.toVisible()
            commentDialog.listComment?.toGone()
        }
        if (result.status != 200) requireContext().showErrorDialog()
    }

    //WHEN AFTER SEND COMMENT
    override fun getDataComment(position: Int, postId: String, result: CommentResponse) {
        val comment = result.data?.get(0)
        val totalComment = result.total_comments
        redeem(postId, "comment")
        itemList[position].comments_count = totalComment
        adapter.notifyItemChanged(position)
        comment?.let {
            itemListComment.add(comment)
            adapterComment.notifyDataSetChanged()
            updateListComment(postId)
        }
        if (itemListComment.isNotEmpty()) {
            commentDialog.emptyComment?.toGone()
            commentDialog.listComment?.toVisible()
        }
        if (result.status != 200) requireContext().showErrorDialog()
    }

    override fun getDataNextComment(result: CommentResponse) {
        val data = result.data
        if (!data.isNullOrEmpty()) {
            itemListComment.addAll(data)
            adapterComment.notifyDataSetChanged()
        }
        if (result.status != 200) requireContext().showErrorDialog()
    }

    override fun getDeletedComment(
        postId: String,
        positionComment: Int,
        positionPost: Int,
        result: CommentResponse
    ) {
        if (itemListComment.isEmpty()) {
            commentDialog.emptyComment?.toVisible()
            commentDialog.listComment?.toGone()
        } else {
            commentDialog.emptyComment?.toGone()
            commentDialog.listComment?.toVisible()
        }
        val totalComment = result.total_comments
        itemList[positionPost].comments_count = totalComment
        adapter.notifyItemChanged(positionPost)
        childFragmentManager.findFragmentByTag("ReplyCommentDialog.tag")?.let {
            (it as ReplyCommentListDialog).dismiss()
        }
        updateListComment(postId)
    }

    private fun showReplyCommentDialog(
        postId: String,
        posPost: Int,
        posComment: Int,
        item: CommentData,
        isReply: Boolean? = null
    ) {
        ReplyCommentListDialog.show(
            childFragmentManager, item, getUserId(), postId, posPost, posComment, isAdmin, isReply,
            onOptionClick = {
                showDeleteCommentDialog(postId, posComment, posPost, item)
            },
            callbackLike = { totalLikeComment: String?, totalLikePost: String, likeStatus: String? ->
                itemList[posPost].likes_count = totalLikePost
                adapter.notifyItemChanged(posPost)

                totalLikeComment?.let { itemListComment[posComment].likes_count = totalLikeComment }
                if (!likeStatus.isNullOrEmpty()) {
                    itemListComment[posComment].like_status = likeStatus
                }
                adapterComment.notifyItemChanged(posComment)
            },
            callbackComment = { totalCommentPost: Int, totalParentComment: Int ->
                itemList[posPost].comments_count = totalCommentPost.toString()
                adapter.notifyItemChanged(posPost)
                itemListComment[posComment].comments_count = totalParentComment.toString()
                adapterComment.notifyItemChanged(posComment)
            }
        )
    }

    private fun showDeleteCommentDialog(
        postId: String,
        positionComment: Int,
        positionPost: Int,
        item: CommentData
    ) {
        DeleteCommentDialog.show(
            childFragmentManager
        ) {
            presenter.deleteComment(
                this,
                postId,
                positionComment,
                positionPost,
                DeleteCommentFeedRequest(getUserId(), item.comment_id)
                , token.toString()
            )
            itemListComment.remove(item)
            adapterComment.notifyDataSetChanged()
        }
    }

    private fun goToComment(position: Int, postId: String) {
        getListComment(postId)
        currentPageComment = 1
        commentDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commentDialog.show()
        commentDialog.listComment?.layoutManager = LinearLayoutManager(requireContext())
        adapterComment = CommentListAdapter(
            requireContext(),
            getUserId(),
            position,
            isAdmin,
            itemListComment,
            onLikeClick = { posComment: Int, posPost: Int, item: CommentData ->
                likeComment(posPost, posComment, item.comment_id)
            },
            onReplyClick = { posComment: Int, posPost: Int, item: CommentData ->
                showReplyCommentDialog(postId, posPost, posComment, item, true)
            },
            onSeeReplyClick = { posComment: Int, posPost: Int, item: CommentData ->
                showReplyCommentDialog(postId, posPost, posComment, item)
            },
            onOptionClick = { posComment: Int, posPost: Int, item: CommentData ->
                showDeleteCommentDialog(
                    postId,
                    posComment,
                    posPost,
                    item
                )
            }
        )
        commentDialog.listComment?.adapter = adapterComment
        commentDialog.scrollComment.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY >= v.getChildAt(v.childCount - 1)
                        .measuredHeight - v.measuredHeight &&
                    scrollY > oldScrollY && currentPageComment < totalPageComment
                ) {
                    currentPageComment++
                    getNextComment(postId, currentPageComment)
                }
            }
        }
        commentDialog.buttonSend?.setOnClickListener {
            val comment = commentDialog.editComment?.text.toString()
            if (comment.isNotEmpty()) {
                sendComment(position, postId, comment)
            }
        }
    }

    private fun getListComment(postId: String) {
        val request = CommentListFeedRequest(postId, 1, getUserId())
        presenter.getListComment(this, request)
    }

    private fun updateListComment(postId: String) {
        currentPageComment = 1
        val request = CommentListFeedRequest(postId, 1, getUserId())
        presenter.updateListComment(this, request)
    }

    private fun likeComment(positionPost: Int, positionComment: Int, commentId: String) {
        val request = LikeCommentRequest(getUserId(), commentId)
        presenter.likeOnComment(this, positionPost, positionComment, request, token.toString())
    }

    private fun getNextComment(postId: String, page: Int) {
        val request = CommentListFeedRequest(postId, page, getUserId())
        presenter.getNextListComment(this, request)
    }

    private fun sendComment(position: Int, postId: String, comment: String) {
        commentDialog.editComment?.setText("")
        val request = CommentFeedRequest(getUserId(), postId, comment)
        presenter.commentFeed(this, postId, position, request, token.toString())
    }

    private fun redeem(postId: String, type: String) {
        val dataLike = tinyDb?.getObject(TINY_DATA_COMMENT_LIKE, DailyTask::class.java)
        if (dataLike != null) {
            if (dataLike.userTaskCompletion < 10) {
                val list = ArrayList<Task>()
                val task = Task(postId, dataLike.taskId.toString(), type)
                list.plusAssign(task)
                val request = RedeemRequest(list, getUserId())
                presenter.redeem(this, request, "")
            }
        }
    }

    override fun getData(result: RedeemResponse) {
        val dataLike = tinyDb?.getObject(TINY_DATA_COMMENT_LIKE, DailyTask::class.java) as DailyTask
        val completion = dataLike.userTaskCompletion
        if (result.status == 200) {
            val data = DailyTask(dataLike.taskId, dataLike.taskLimit, completion + 1)
            tinyDb?.putObject(TINY_DATA_COMMENT_LIKE, data)
            (activity as HomeActivity).setBadge()
        }
    }

    /*Deep Link*/

    private fun gotoOrderDetail(orderId: String) {
        val intent = Intent(requireActivity(), OrderDetailActivity::class.java)
        intent.putExtra("ORDER_ID", orderId)
        requireActivity().startActivity(intent)
    }

    private fun validateDeepLink() {
        statusPage = "shared-page"
        val value: String = tinyDb?.getString(TINY_DEEP_LINK).toString()
        val request = DetailFeedRequest()
        request.post_id = value
        presenter.getDetailFeed(this, request)
    }

    private fun validateNotification() {
        statusPage = "push-notification"
        val value: String = tinyDb?.getString("post_id").toString()
        val request = DetailFeedRequest()
        request.post_id = value
        presenter.getDetailFeed(this, request)
    }

    private fun createDynamicLink(id: String) {
        val loading = LoaderDialogHelper.getInstance()
        loading.showDialog(requireContext())
        val userId = getUserId()
        val link = "$URL_DEEP_LINK/?post_id=$id&?utm_source=spin&utm_medium=share&utm_campaign=$userId"
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(link))
            .setDomainUriPrefix(DOMAIN_PREFIX_DEEP_LINK)
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                    .setMinimumVersion(1)
                    .build()
            )
            .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
            .addOnSuccessListener { shortDynamicLink ->
                loading.dismissDialog()
                val invitationUrl = shortDynamicLink.shortLink.toString()
                sendDynamicLink(invitationUrl)
            }
            .addOnFailureListener {
                activity?.showOkDialog(it.message.toString(), "Oke", null)
            }
    }

    private fun sendDynamicLink(invitationUrl: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, invitationUrl)
        }
        startActivity(intent)
    }

    override fun getData(result: DetailFeedResponse) {
        TinyDB(requireContext()).remove(TINY_DEEP_LINK)
        if (result.status == 200) {
            for (x in result.data!!.indices) {
                setDetailFeed(result.data[x])
            }
        } else {
            requireContext().showErrorDialog()
        }
    }

    /*State*/

    private var isAdmin = false
    private fun getUserId(): String {
        val data = TinyDB(requireContext()).getObject(
            TinyConstant.TINY_PROFILE, LoginData::class.java
        )
        return if (data != null) {
            isAdmin = data.role?.equals("administrator") ?: false
            val userId = data.userId
            if (userId != null) userId
            else ""
        } else ""
    }

    private fun getSelectedIdGames(): ArrayList<String> {
        listNameGame.clear()
        val idListGame = ArrayList<String>()
        val idListNameGame = ArrayList<String>()
        val itemList = ArrayList<GameData>()
        itemList.clear()
        itemList.addAll(
            TinyDB(requireContext()).getListObject(
                TinyConstant.TINY_LIST_GAME, GameData::class.java
            ) as ArrayList<GameData>
        )
        for (item in itemList) {
            if (item.isSelected) {
                idListGame.add(item.id)
                idListNameGame.plusAssign(item.name)
            }
        }
        listNameGame.plusAssign(idListNameGame)
        return idListGame
    }

    private fun getSelectedIdTopic(): ArrayList<String> {
        listNameTopic.clear()
        val idListTopic = ArrayList<String>()
        val idListNameTopic = ArrayList<String>()
        val itemList = ArrayList<TopicData>()
        itemList.clear()
        itemList.addAll(
            TinyDB(requireContext()).getListObject(
                TinyConstant.TINY_LIST_TOPIC, TopicData::class.java
            ) as ArrayList<TopicData>
        )
        for (item in itemList) {
            if (item.isSelected) {
                idListTopic.add(item.id)
                idListNameTopic.plusAssign(item.name)
            }
        }
        listNameTopic.plusAssign(idListNameTopic)
        return idListTopic
    }

    private fun validateLogin(code: Int, pos: Int, data: FeedData) {
        if (isLogin == true) {
            when (code) {
                2 -> {
                    context?.showShortToast("Option Click")
                }
                3 -> {
                    likeFeed(pos, data.post_id.toString())
                    trackerItemFeed(2, data)
                }
                4 -> {
                    goToComment(pos, data.post_id.toString())
                    trackerItemFeed(4, data)
                }
                5 -> {
                    createDynamicLink(data.post_id.toString())
                    trackerItemFeed(3, data)
                }
            }
        } else {
            when (code) {
                2 -> {
                    trackerOnBoarding("Option")
                }
                3 -> {
                    trackerOnBoarding("Like")
                }
                4 -> {
                    trackerOnBoarding("Comment")
                }
                5 -> {
                    trackerOnBoarding("Share")
                }
            }
            activity?.goToActivity(BoardingActivity::class.java)
        }
    }

    private fun validateLoginDetail(code: Int, data: DataItem?) {
        if (isLogin == true) {
            when (code) {
                2 -> {
                    context?.showShortToast("Option Click")
                }
                3 -> {
                    if (data != null) {
                        likeFeed(data)
                        trackerDetailFeed(2, data)
                    }
                }
                4 -> {
                    if (data != null) {
                        goToComment(0, data.postId.toString())
                        trackerDetailFeed(3, data)
                    }
                }
                5 -> {
                    if (data != null) {
                        createDynamicLink(data.postId.toString())
                        trackerDetailFeed(4, data)
                    }
                }
            }
        } else {
            when (code) {
                2 -> {
                    trackerOnBoarding("Option")
                }
                3 -> {
                    trackerOnBoarding("Like")
                }
                4 -> {
                    trackerOnBoarding("Comment")
                }
                5 -> {
                    trackerOnBoarding("Share")
                }
            }
            activity?.goToActivity(BoardingActivity::class.java)
        }
    }

    private fun goToWebView(
        url: String,
        title: String,
        baseUrl: String,
        postId: String,
        like: String,
        comment: String,
        data: FeedData?
    ) {
        if (data != null) {
            val view = DataImpression("click", postId)
            listImpression.plusAssign(view)
            if (listImpression.size > 5) {
                setImpression()
            }
            WebviewBuilder(requireContext())
                .setUrl(url)
                .setTitle(title)
                .setLike(like)
                .setDataFeed(data)
                .setComment(comment)
                .setBaseUrl(baseUrl)
                .setVersionCode(postId)
                .setHttpMethod(HttpMethodConstant.Value.GET)
                .setIsDebug(true)
                .setAppSource(AppSourceConstant.Value.SPIN_ANDROID)
                .show()
        } else {
            WebviewBuilder(requireContext())
                .setUrl(url)
                .setTitle(title)
                .setLike(like)
                .setComment(comment)
                .setBaseUrl(baseUrl)
                .setVersionCode(postId)
                .setHttpMethod(HttpMethodConstant.Value.GET)
                .setIsDebug(true)
                .setAppSource(AppSourceConstant.Value.SPIN_ANDROID)
                .show()
        }
    }

    /*Amplitude & Impression*/


    private fun setImpression() {
        val deviceId =
            Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
        val dataImpression = tinyDb?.getListObject(
            TINY_LIST_IMPRESSION,
            DataImpression::class.java
        ) as ArrayList<DataImpression>
        listImpression.plusAssign(dataImpression)
        val request = ImpressionRequest(deviceId, listImpression)
        presenter.impression(this, request)
    }

    override fun getDataImpression(result: ImpressionResponse) {

    }

    private fun trackerItemFeed(status: Int, data: FeedData) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Post ID", data.post_id)
            eventProperties.put("Post Description", data.caption)
            eventProperties.put("Post Game Main Category", data.game_name)
            eventProperties.put("Post Topic Main Category", data.topic_name)
            eventProperties.put("Post Moderator", "admin")
            eventProperties.put("Post Creator", data.publisher_name)
            eventProperties.put("Post Platform", data.platform_name)
            eventProperties.put("Post Schedule", data.publishing_schedule)
            eventProperties.put("Post Source", "Feed")
            eventProperties.put("Post Rank", data.trending_id)
        } catch (exception: JSONException) {
        }

        when (status) {
            1 -> tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_CLICKED, eventProperties)
            2 -> tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_LIKED, eventProperties)
            3 -> tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_SHARED, eventProperties)
            4 -> tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_COMMENTED, eventProperties)
        }
    }

    private fun trackerDetailFeed(status: Int, data: DataItem) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Post ID", data.postId)
            eventProperties.put("Post Description", data.caption)
            eventProperties.put("Post Game Main Category", data.gameName)
            eventProperties.put("Post Topic Main Category", data.topicName)
            eventProperties.put("Post Moderator", "admin")
            eventProperties.put("Post Creator", data.publisherName)
            eventProperties.put("Post Platform", data.platformName)
            eventProperties.put("Post Schedule", data.publishingSchedule)
            eventProperties.put("Post Source", "Shared Page")
            eventProperties.put("Post Rank", data.trendingId)
        } catch (exception: JSONException) {
        }

        when (status) {
            1 -> tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_CLICKED, eventProperties)
            2 -> tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_LIKED, eventProperties)
            3 -> tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_SHARED, eventProperties)
            4 -> tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_COMMENTED, eventProperties)
        }
    }

    private fun trackerBanner(code: Int, data: BannerData) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Banner ID", data.id)
            eventProperties.put("Banner URL", data.url)
        } catch (exception: JSONException) {
        }
        when (code) {
            1 -> tracker?.logEvent(PARAM_BANNER_CLICKED, eventProperties)
            2 -> tracker?.logEvent(PARAM_BANNER_SWIPED, eventProperties)
        }
    }

    private fun trackerLoadFeed(code: Int) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Selected Game Category", listNameGame)
            eventProperties.put("Selected Topic Category", listNameTopic)
        } catch (exception: JSONException) {
        }
        when (code) {
            1 -> tracker?.logEvent(PARAM_FEED_LOAD, eventProperties)
            2 -> tracker?.logEvent(PARAM_FEED_LOAD_PAGINATION, eventProperties)
            3 -> tracker?.logEvent(PARAM_FEED_EMPTY, eventProperties)
            4 -> tracker?.logEvent(PARAM_FEED_ERROR, eventProperties)
        }
    }

    private fun trackerOnBoarding(name: String) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Entry Point", name)
        } catch (exception: JSONException) {
        }
        tracker?.logEvent(PARAM_ON_BOARDING_LOAD, eventProperties)
    }

    override fun showError(throwable: Throwable) {
        showErrorLayout()
        Log.e("ERROR THROWABLE", throwable.message.toString())
        textTitleError.text = getString(R.string.error_internet_label)
        textTitleError.text = getString(R.string.suberror_internet_label)
    }

    override fun showErrorComment(throwable: Throwable) {
        context?.showShortToast(throwable.message.toString())
    }

    override fun showLoadingComment() {
        commentDialog.progressBar?.toVisible()
        commentDialog.emptyComment?.toGone()
        commentDialog.listComment?.toGone()
    }

    override fun hideLoadingComment() {
        commentDialog.progressBar?.toGone()
    }

    private fun showErrorLayout() {
        layoutError.toVisible()
        trackerLoadFeed(4)
    }

    private fun hideErrorLayout() {
        layoutError.toGone()
    }

    override fun showProgress() {
        progress?.showDialog(requireContext())
    }

    override fun hideProgress() {
        progress?.dismissDialog()
    }

    override fun onPause() {
        super.onPause()
        FeedPresenter.onRefresh = null
        setImpression()
    }

    override fun onResume() {
        super.onResume()
        if (FeedPresenter.onRefresh == true) {
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FeedPresenter.onRefresh = null
    }
}
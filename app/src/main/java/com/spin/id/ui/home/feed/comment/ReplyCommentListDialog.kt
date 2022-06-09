package com.spin.id.ui.home.feed.comment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.spin.id.R
import com.spin.id.api.request.feed.ChildCommentFeedRequest
import com.spin.id.api.request.feed.CommentOnCommentRequest
import com.spin.id.api.request.feed.DeleteCommentFeedRequest
import com.spin.id.api.request.feed.LikeCommentRequest
import com.spin.id.api.response.feed.comment.ChildCommentData
import com.spin.id.api.response.feed.comment.ChildCommentResponse
import com.spin.id.api.response.feed.comment.CommentData
import com.spin.id.api.response.feed.comment.CommentResponse
import com.spin.id.api.response.feed.like.LikeCommentResponse
import com.spin.id.preference.tinyDb.TinyConstant.TINY_TOKEN
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.home.feed.FeedContract
import com.spin.id.ui.home.feed.FeedPresenter
import com.spin.id.utils.TimeUtils
import com.spin.id.utils.base.BaseBottomSheetDialog
import com.spin.id.utils.extensions.showShortToast
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.bottom_sheet_toolbar.*
import kotlinx.android.synthetic.main.comment_item.*
import kotlinx.android.synthetic.main.fragment_reply_comment_dialog.*
import java.util.concurrent.TimeUnit

class ReplyCommentListDialog : BaseBottomSheetDialog(), FeedContract.ChildCommentListView,
    FeedContract.ChildNextCommentView, FeedContract.ChildCommentView, FeedContract.LikeCommentView,
    FeedContract.LikeChildCommentView, FeedContract.DeleteCommentFeedView {

    private lateinit var presenter: FeedPresenter
    private lateinit var item: CommentData
    private lateinit var userId: String
    private lateinit var postId: String
    private lateinit var token: String
    private var onOptionClick: (() -> Unit)? = null
    private var callbackLike: ((String?, String, String?) -> Unit)? = null
    private var callbackComment: ((Int, Int) -> Unit)? = null
    private var isReply: Boolean? = false
    private var isAdmin: Boolean = false
    private lateinit var adapter: ReplyCommentListAdapter
    private var itemListChildComment = ArrayList<ChildCommentData>()

    private var positionPost = 0
    private var positionComment = 0
    private var currentPageComment = 1
    private var totalPageComment = 0


    companion object {
        fun show(
            fm: FragmentManager,
            item: CommentData,
            userId: String,
            postId: String,
            positionPost: Int,
            positionComment: Int,
            isAdmin: Boolean,
            isReply: Boolean? = false,
            onOptionClick: (() -> Unit)? = null,
            callbackLike: ((String?, String, String?) -> Unit)? = null,
            callbackComment: ((Int, Int) -> Unit)? = null
        ) = ReplyCommentListDialog().apply {
            this.item = item
            this.userId = userId
            this.postId = postId
            this.positionPost = positionPost
            this.positionComment = positionComment
            this.isReply = isReply
            this.isAdmin = isAdmin
            this.onOptionClick = onOptionClick
            this.callbackLike = callbackLike
            this.callbackComment = callbackComment
        }.show(fm, "ReplyCommentDialog.tag")
    }

    override fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reply_comment_dialog, container, false)
    }

    override fun init(bundle: Bundle?) {
        presenter = FeedPresenter()
        token = TinyDB(requireContext()).getString(TINY_TOKEN).toString()
        adapter = ReplyCommentListAdapter(
            requireContext(),
            userId,
            isAdmin,
            itemListChildComment,
            onLikeClick = { pos: Int, item: ChildCommentData ->
                likeChildComment(pos, item.comment_id)
            },
            onReplyClick = { pos: Int, item: ChildCommentData ->
                requestReply(true, item.username)
            },
            onOptionClick = { pos: Int, item: ChildCommentData ->
                deleteComment(pos, item)
            })
        initUI()
    }

    override fun initData(bundle: Bundle?) {
        loadChildComment(item.comment_id)
    }

    @SuppressLint("CheckResult")
    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener {
            dismiss()
        }
        buttonSend?.setOnClickListener {
            val comment = editComment?.text.toString()
            if (comment.isNotEmpty()) {
                sendComment(userId, postId, item.comment_parent_id, comment)
            }
        }
        editComment.afterTextChangeEvents()
            .skip(1)
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                textCounter.text =
                    getString(R.string.counter_comment, it.editable?.length.toString())
                editComment.filters += InputFilter.LengthFilter(500)
            }

        scrollChildComment.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY >= v.getChildAt(v.childCount - 1)
                        .measuredHeight - v.measuredHeight &&
                    scrollY > oldScrollY && currentPageComment < totalPageComment
                ) {
                    currentPageComment++
                    loadNextChildComment(item.comment_id, currentPageComment)
                }
            }
        }
        textInformationReply?.setOnClickListener {
            textInformationReply?.toGone()
            editComment?.setText("")
        }
    }

    private fun initUI() {
        requestReply(isReply == true, item.username)
        textName?.text = item.username
        if (item.role.equals("admin", true)) {
            textAdmin?.toVisible()
        } else {
            textAdmin?.toGone()
        }
        textCaption?.text = item.comment
        buttonLike?.text = item.likes_count
        textTime?.text = TimeUtils.setTimeAgo(requireContext(), item.created_at)
        if (item.user_id.equals(userId) || isAdmin) {
            buttonOptionComment?.toVisible()
        } else {
            buttonOptionComment?.toGone()
        }
        setLikeStatus()
        listSubComment?.adapter = adapter
        listSubComment?.layoutManager = LinearLayoutManager(context)

        buttonLike?.setOnClickListener {
            likeComment(positionPost, positionComment, item.comment_id)
        }
        buttonReply?.setOnClickListener {
            requestReply(true, item.username)
        }
        buttonOptionComment?.setOnClickListener {
            onOptionClick?.invoke()
        }
    }

    private fun sendComment(
        userId: String,
        postId: String,
        commentParentId: String,
        comment: String
    ) {
        editComment?.setText("")
        textInformationReply?.toGone()
        presenter.commentOnComment(
            this,
            CommentOnCommentRequest(userId, postId, commentParentId, comment)
            , token.toString()
        )
    }

    private fun deleteComment(
        positionChildComment: Int,
        item: ChildCommentData
    ) {
        DeleteCommentDialog.show(
            childFragmentManager
        ) {
            itemListChildComment.remove(item)
            adapter.notifyDataSetChanged()
            presenter.deleteComment(
                this,
                postId,
                positionComment,
                positionPost,
                DeleteCommentFeedRequest(userId, item.comment_id)
                , token
            )
        }
    }

    private fun loadChildComment(commentId: String) {
        val request = ChildCommentFeedRequest(userId, 1, commentId)
        presenter.getChildComment(this, request)
    }

    private fun updateChildComment(commentId: String) {
        currentPageComment = 1
        val request = ChildCommentFeedRequest(userId, 1, commentId)
        presenter.updateChildComment(this, request)
    }

    private fun loadNextChildComment(commentId: String, page: Int) {
        val request = ChildCommentFeedRequest(userId, page, commentId)
        presenter.getNextChildComment(this, request)
    }

    @SuppressLint("SetTextI18n")
    private fun requestReply(isReply: Boolean, username: String) {
        if (isReply) {
            textInformationReply?.toVisible()
            textInformationReply?.text = context?.getString(R.string.reply_comment_label, username)
            editComment?.setText("@${username} ")
            editComment?.requestFocus()
        }
    }

    private fun likeComment(positionPost: Int, positionComment: Int, commentId: String) {
        val request = LikeCommentRequest(userId, commentId)
        presenter.likeOnComment(this, positionPost, positionComment, request, token)
    }

    private fun likeChildComment(positionChildComment: Int, commentId: String) {
        val request = LikeCommentRequest(userId, commentId)
        presenter.likeOnChildComment(this, positionChildComment, request, token)
    }

    private fun setLikeStatus() {
        if (item.like_status.equals("ACTIVE", true)) {
            buttonLike?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0)
        } else {
            buttonLike?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unlike, 0, 0, 0)
        }
    }

    override fun showLoadingComment() {
        progressBar?.toVisible()
    }

    override fun hideLoadingComment() {
        progressBar?.toGone()
    }

    override fun getDataChildComment(result: ChildCommentResponse) {
        val data = result.data
        totalPageComment = result.total_pages
        itemListChildComment.clear()
        if (!data.isNullOrEmpty()) {
            itemListChildComment.addAll(data)
            adapter.totalSize = result.total_items
            adapter.notifyDataSetChanged()
        }
    }


    override fun getNextChildComment(result: ChildCommentResponse) {
        val data = result.data
        if (!data.isNullOrEmpty()) {
            itemListChildComment.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }

    override fun successAddComment(result: ChildCommentResponse) {
        val data = result.data?.get(0)
        val totalComment = result.total_comments
        val totalChildComment = result.total_child_comments
        data?.let {
            itemListChildComment.add(data)
        }
        callbackComment?.invoke(totalComment, totalChildComment)
        adapter.notifyDataSetChanged()
        updateChildComment(item.comment_id)
    }

    override fun showError(throwable: Throwable) {
        context?.showShortToast(getString(R.string.conn_no_connection_message))
    }

    override fun getTotalLikeComment(
        positionPost: Int,
        positionComment: Int,
        result: LikeCommentResponse
    ) {
        val totalLikeComment = result.total_comment_likes
        val totalLikePost = result.total_post_likes
        val likeStatus = result.data[0].like_status

        item.like_status = likeStatus
        buttonLike?.text = totalLikeComment
        setLikeStatus()
        callbackLike?.invoke(totalLikeComment, totalLikePost, likeStatus)
    }

    override fun getTotalLikeComment(positionChildComment: Int, result: LikeCommentResponse) {
        val totalLikeComment = result.total_comment_likes
        val totalLikePost = result.total_post_likes
        val likeStatus = result.data[0].like_status

        itemListChildComment[positionChildComment].like_status = likeStatus
        itemListChildComment[positionChildComment].likes_count = totalLikeComment
        adapter.notifyItemChanged(positionChildComment)
        callbackLike?.invoke(null, totalLikePost, null)
    }

    override fun showErrorLike(throwable: Throwable) {
        context?.showShortToast(getString(R.string.conn_no_connection_message))
    }

    override fun getDeletedComment(
        postId: String,
        positionComment: Int,
        positionPost: Int,
        result: CommentResponse
    ) {
        val totalComment = result.total_comments
        val totalChildComment = itemListChildComment.size
        callbackComment?.invoke(totalComment.toInt(), totalChildComment)
        updateChildComment(item.comment_id)
        Log.e("RESULT DELETE COMMENT", Gson().toJson(result))
    }

    override fun showErrorComment(throwable: Throwable) {
        context?.showShortToast(getString(R.string.conn_no_connection_message))
    }
}
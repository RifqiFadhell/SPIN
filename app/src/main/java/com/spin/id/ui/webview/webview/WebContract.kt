package com.spin.id.ui.webview.webview

import com.spin.id.api.response.feed.comment.ChildCommentResponse
import com.spin.id.api.response.feed.comment.CommentResponse
import com.spin.id.api.response.feed.like.LikeCommentResponse
import com.spin.id.api.response.feed.like.LikeFeedResponse

class WebContract {

    interface LikeFeedView {
        fun getTotalLike(result: LikeFeedResponse)
        fun showErrorLike(throwable: Throwable)
    }

    interface CommentListFeedView {
        fun showLoadingComment()
        fun hideLoadingComment()
        fun getDataComment(result: CommentResponse)
        fun showErrorComment(throwable: Throwable)
    }

    interface CommentListPageView {
        fun getDataNextComment(result: CommentResponse)
    }

    interface CommentFeedView {
        fun getDataComment(position: Int, postId: String, result: CommentResponse)
        fun showErrorComment(throwable: Throwable)
    }

    interface DeleteCommentFeedView {
        fun getDeletedComment(
            postId: String,
            positionComment: Int,
            positionPost: Int,
            result: CommentResponse
        )

        fun showErrorComment(throwable: Throwable)
    }

    interface ChildCommentListView {
        fun showLoadingComment()
        fun hideLoadingComment()
        fun getDataChildComment(result: ChildCommentResponse)
        fun showError(throwable: Throwable)
    }

    interface ChildNextCommentView {
        fun getNextChildComment(result: ChildCommentResponse)
    }

    interface ChildCommentView {
        fun successAddComment(result: ChildCommentResponse)
        fun showError(throwable: Throwable)
    }

    interface LikeCommentView {
        fun getTotalLikeComment(
            positionPost: Int,
            positionComment: Int,
            result: LikeCommentResponse
        )

        fun showErrorLike(throwable: Throwable)
    }

    interface LikeChildCommentView {
        fun getTotalLikeComment(positionChildComment: Int, result: LikeCommentResponse)
        fun showErrorLike(throwable: Throwable)
    }
}
package com.spin.id.ui.home.feed

import com.spin.id.api.response.banner.BannerResponse
import com.spin.id.api.response.feed.comment.ChildCommentResponse
import com.spin.id.api.response.feed.comment.CommentResponse
import com.spin.id.api.response.feed.detail.DetailFeedResponse
import com.spin.id.api.response.feed.like.LikeCommentResponse
import com.spin.id.api.response.feed.like.LikeFeedResponse
import com.spin.id.api.response.feed.list.FeedResponse
import com.spin.id.api.response.impression.ImpressionResponse
import com.spin.id.api.response.mission.redeem.RedeemResponse

class FeedContract {

    interface BannerView {
        fun getData(result: BannerResponse)
        fun showError(throwable: Throwable)
    }

    interface FeedView {
        fun showProgress()
        fun hideProgress()
        fun getData(result: FeedResponse)
        fun showError(throwable: Throwable)
    }

    interface Redeem {
        fun getData(result: RedeemResponse)
        fun showError(throwable: Throwable)
    }

    interface Impression {
        fun getDataImpression(result: ImpressionResponse)
        fun showError(throwable: Throwable)
    }

    interface DetailFeedView {
        fun showProgress()
        fun hideProgress()
        fun getData(result: DetailFeedResponse)
        fun showError(throwable: Throwable)
    }

    interface FeedPageView {
        fun getDataNext(result: FeedResponse)
    }

    interface LikeFeedView {
        fun getTotalLike(position: Int, result: LikeFeedResponse)
        fun showErrorLike(throwable: Throwable)
    }

    interface LikeFeedDetailView {
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
        fun getDeletedComment(postId: String, positionComment: Int, positionPost: Int, result: CommentResponse)
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

    interface LikeCommentView{
        fun getTotalLikeComment(positionPost: Int, positionComment: Int, result: LikeCommentResponse)
        fun showErrorLike(throwable: Throwable)
    }

    interface LikeChildCommentView{
        fun getTotalLikeComment(positionChildComment: Int, result: LikeCommentResponse)
        fun showErrorLike(throwable: Throwable)
    }
}
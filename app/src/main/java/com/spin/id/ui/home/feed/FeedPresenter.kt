package com.spin.id.ui.home.feed

import com.spin.id.CustomApplication
import com.spin.id.api.request.banner.BannerRequest
import com.spin.id.api.request.feed.*
import com.spin.id.api.request.impression.DataImpression
import com.spin.id.api.request.impression.ImpressionRequest
import com.spin.id.api.request.redeem.RedeemRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class FeedPresenter {

    companion object {
        var onRefresh: Boolean? = null
    }

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun getListBanner(view: FeedContract.BannerView, user_id: String? = null) {
        disposable = apiFactory?.getListBanner(BannerRequest(user_id))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getData(result)
            }, {
                view.showError(it)
            })

    }

    fun getListFeed(view: FeedContract.FeedView, request: FeedRequest) {
        disposable = apiFactory?.feedList(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.hideProgress()
                view.getData(result)
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun getDetailFeed(view: FeedContract.DetailFeedView, request: DetailFeedRequest) {
        disposable = apiFactory?.getDetailFeed(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getData(result)
            }, {
                view.showError(it)
            })
    }

    fun getNextListFeed(view: FeedContract.FeedPageView, request: FeedRequest) {
        disposable = apiFactory?.feedList(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataNext(result)
            }, {
            })
    }

    fun redeem(view: FeedContract.Redeem, request: RedeemRequest, token: String) {
        disposable = apiFactory?.redeem(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getData(result)
            }, {
                view.showError(it)
            })
    }

    fun impression(view: FeedContract.Impression, request: ImpressionRequest) {
        val list = ArrayList<DataImpression>()
        request.data?.let { list.addAll(it) }
        disposable = apiFactory?.impression(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataImpression(result)
            }, {
                view.showError(it)
            })
    }

    fun likeFeed(
        view: FeedContract.LikeFeedView,
        position: Int,
        request: LikeFeedRequest,
        token: String
    ) {
        disposable = apiFactory?.likeFeed(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getTotalLike(position, result)
            }, {
                view.showErrorLike(it)
            })
    }

    fun likeDetailFeed(view: FeedContract.LikeFeedDetailView, request: LikeFeedRequest, token: String) {
        disposable = apiFactory?.likeFeed(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getTotalLike(result)
            }, {
                view.showErrorLike(it)
            })
    }

    fun getListComment(view: FeedContract.CommentListFeedView, request: CommentListFeedRequest) {
        view.showLoadingComment()
        disposable = apiFactory?.getListComment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.hideLoadingComment()
                view.getDataComment(it)
            }, {
                view.hideLoadingComment()
                view.showErrorComment(it)
            })
    }

    fun updateListComment(view: FeedContract.CommentListFeedView, request: CommentListFeedRequest) {
        disposable = apiFactory?.getListComment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getDataComment(it)
            }, {
            })
    }

    fun getNextListComment(view: FeedContract.CommentListPageView, request: CommentListFeedRequest){
        disposable = apiFactory?.getListComment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataNextComment(result)
            }, {
            })
    }
    fun commentFeed(
        view: FeedContract.CommentFeedView,
        postId: String,
        position: Int,
        request: CommentFeedRequest, token: String
    ) {
        disposable = apiFactory?.commentFeed(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataComment(position, postId, result)
            }, {
                view.showErrorComment(it)
            })
    }

    fun deleteComment(
        view: FeedContract.DeleteCommentFeedView,
        postId: String,
        positionComment: Int,
        positionPost: Int,
        request: DeleteCommentFeedRequest, token: String
    ) {
        disposable = apiFactory?.deleteCommentFeed(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDeletedComment(postId, positionComment, positionPost, result)
            }, {
                view.showErrorComment(it)
            })
    }

    fun getChildComment(
        view: FeedContract.ChildCommentListView,
        request: ChildCommentFeedRequest
    ) {
        view.showLoadingComment()
        disposable = apiFactory?.getListChildComment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.hideLoadingComment()
                view.getDataChildComment(it)
            }, {
                view.hideLoadingComment()
                view.showError(it)
            })
    }

    fun updateChildComment(
        view: FeedContract.ChildCommentListView,
        request: ChildCommentFeedRequest
    ) {
        disposable = apiFactory?.getListChildComment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getDataChildComment(it)
            }, {
            })
    }

    fun getNextChildComment(
        view: FeedContract.ChildNextCommentView,
        request: ChildCommentFeedRequest
    ) {
        disposable = apiFactory?.getListChildComment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getNextChildComment(it)
            }, {
            })
    }

    fun commentOnComment(
        view: FeedContract.ChildCommentView,
        request: CommentOnCommentRequest, token: String
    ) {
        disposable = apiFactory?.commentOnComment(token ,request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.successAddComment(it)
            }, {
                view.showError(it)
            })

    }

    fun likeOnComment(
        view:FeedContract.LikeCommentView,
        positionPost: Int,
        positionComment: Int,
        request: LikeCommentRequest, token: String
    ){
        disposable = apiFactory?.likeComment(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getTotalLikeComment(positionPost, positionComment, it)
            }, {
                view.showErrorLike(it)
            })
    }

    fun likeOnChildComment(
        view:FeedContract.LikeChildCommentView,
        positionChildComment: Int,
        request: LikeCommentRequest, token: String
    ){
        disposable = apiFactory?.likeComment(token ,request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getTotalLikeComment(positionChildComment, it)
            }, {
                view.showErrorLike(it)
            })
    }
}
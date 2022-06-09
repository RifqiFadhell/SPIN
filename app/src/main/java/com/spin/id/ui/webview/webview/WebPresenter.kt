package com.spin.id.ui.webview.webview

import com.spin.id.CustomApplication
import com.spin.id.api.request.banner.BannerRequest
import com.spin.id.api.request.feed.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class WebPresenter {

    companion object {
        var onRefresh: Boolean? = null
    }

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun likeFeed(view: WebContract.LikeFeedView, request: LikeFeedRequest, token: String) {
        disposable = apiFactory?.likeFeed(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getTotalLike(result)
            }, {
                view.showErrorLike(it)
            })
    }

    fun getListComment(view: WebContract.CommentListFeedView, request: CommentListFeedRequest) {
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

    fun updateListComment(view: WebContract.CommentListFeedView, request: CommentListFeedRequest) {
        disposable = apiFactory?.getListComment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getDataComment(it)
            }, {
            })
    }

    fun getNextListComment(view: WebContract.CommentListPageView, request: CommentListFeedRequest){
        disposable = apiFactory?.getListComment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataNextComment(result)
            }, {
            })
    }
    fun commentFeed(
        view: WebContract.CommentFeedView,
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
        view: WebContract.DeleteCommentFeedView,
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
        view: WebContract.ChildCommentListView,
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
        view: WebContract.ChildCommentListView,
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
        view: WebContract.ChildNextCommentView,
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
        view: WebContract.ChildCommentView,
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
        view:WebContract.LikeCommentView,
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
        view:WebContract.LikeChildCommentView,
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
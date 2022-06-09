package com.spin.id.ui.home.profile

import com.spin.id.CustomApplication
import com.spin.id.api.request.order.OrderRequest
import com.spin.id.api.request.profile.EditUserNameRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ProfilePresenter {

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun editProfile(view: ProfileContract.View, request: EditUserNameRequest, token: String) {
        disposable = apiFactory?.editProfile(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getData(it)
            }, {
                view.showError(it)
            })
    }

    fun getTotalActiveOrder(view: ProfileContract.View, userId: String) {
        disposable = apiFactory?.getHistoryOrder(OrderRequest(userId))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                val data = result.data.counts
                view.getTotalActiveOrder(data)
            }, {
            })
    }
}
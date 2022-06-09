package com.spin.id.ui.order

import com.spin.id.CustomApplication
import com.spin.id.api.request.order.DetailOrderRequest
import com.spin.id.api.request.order.OrderRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class OrderPresenter {
    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun getHistoryOrder(view: OrderContract.OrderListView, userId: String) {
        view.showLoading()
        disposable = apiFactory?.getHistoryOrder(OrderRequest(userId))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.hideLoading()
                view.getDataOrderList(result)
            }, {
                view.hideLoading()
                view.getErrorOrderList(it)
            })
    }

    fun getDetailOrder(view: OrderContract.OrderDetailView, orderId: String) {
        view.showLoading()
        disposable = apiFactory?.getOrder(DetailOrderRequest(orderId))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.hideLoading()
                view.getDataOrder(result)
            }, {
                view.hideLoading()
                view.getErrorOrder(it)
            })
    }

    fun cancelOrder(view: OrderContract.OrderDetailView, orderId: String) {
        view.showLoading()
        disposable = apiFactory?.cancelOrder(DetailOrderRequest(orderId))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.hideLoading()
                view.getDataOrder(result)
            }, {
                view.hideLoading()
                view.getErrorOrder(it)
            })
    }
}
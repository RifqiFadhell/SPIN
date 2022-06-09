package com.spin.id.ui.home.shop.detail

import com.spin.id.CustomApplication
import com.spin.id.api.request.shop.buy.PaymentRequest
import com.spin.id.api.request.shop.detail.DetailProductRequest
import com.spin.id.api.request.shop.discount.DiscountRequest
import com.spin.id.api.request.validation.CheckUserRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class DetailProductPresenter {

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun detailProduct(view: DetailProductContract.View, request: DetailProductRequest) {
        disposable = apiFactory?.detailProduct(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataDetail(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun getPaymentMethod(view: DetailProductContract.View) {
        disposable = apiFactory?.getPaymentMethod()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataPaymentMethod(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun discount(view: DetailProductContract.View, request: DiscountRequest) {
        view.showProgress()
        disposable = apiFactory?.discount(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataDiscount(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun requestPayment(view: DetailProductContract.View, request: PaymentRequest) {
        view.showProgress()
        disposable = apiFactory?.sendPayment(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataPayment(result)
                view.hideProgress()
            }, {
                view.showErrorPayment(it)
                view.hideProgress()
            })
    }

    fun checkValidation(view: DetailProductContract.View, request: CheckUserRequest) {
        disposable = apiFactory?.checkUser(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataValidation(result)
            }, {
                view.showError(it)
            })
    }
}
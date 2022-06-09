package com.spin.id.ui.home.shop

import com.spin.id.CustomApplication
import com.spin.id.api.request.order.OrderRequest
import com.spin.id.api.request.shop.CategoryRequest
import com.spin.id.api.request.shop.ProductCategoryRequest
import com.spin.id.api.request.shop.ProductMasterRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ShopPresenter {
    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun getCategoryProduct(view: ShopContract.ShopView) {
        disposable = apiFactory?.getCategoryProduct(CategoryRequest(""))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataCategory(result)
            }, {
                view.showError(it)
            })
    }

    fun getProduct(view: ShopContract.ShopView, category: String) {
        disposable = apiFactory?.getProduct(ProductCategoryRequest(category))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataProduct(result)
            }, {
                view.showError(it)
            })
    }

    fun getProductItem(view: ShopContract.ItemProductView, productId: Int, sort: String) {
        view.showLoading()
        disposable = apiFactory?.productItemDetail(ProductMasterRequest(productId, sort))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.hideLoading()
                view.getDataItemProduct(result)
            }, {
                view.hideLoading()
                view.showError(it)
            })
    }

    fun getTotalActiveOrder(view: ShopContract.ShopView, userId: String) {
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
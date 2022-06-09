package com.spin.id.ui.home.shop

import com.spin.id.api.response.order.orderlist.OrderCounts
import com.spin.id.api.response.shop.CategoryResponse
import com.spin.id.api.response.shop.ProductCategoryResponse
import com.spin.id.api.response.shop.ProductMasterResponse

class ShopContract {
    interface ShopView {
        fun getDataCategory(result: CategoryResponse)
        fun getDataProduct(result: ProductCategoryResponse)
        fun getTotalActiveOrder(result: OrderCounts)
        fun showError(throwable: Throwable)
    }

    interface ItemProductView {
        fun showLoading()
        fun hideLoading()
        fun getDataItemProduct(result: ProductMasterResponse)
        fun showError(throwable: Throwable)
    }
}
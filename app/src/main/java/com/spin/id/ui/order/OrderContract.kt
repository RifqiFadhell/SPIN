package com.spin.id.ui.order

import com.spin.id.api.response.order.orderdetail.OrderDetailResponse
import com.spin.id.api.response.order.orderlist.OrderResponse

class OrderContract {
    interface OrderListView {
        fun getDataOrderList(result: OrderResponse)
        fun getErrorOrderList(throwable: Throwable)
        fun showLoading()
        fun hideLoading()
    }

    interface OrderDetailView {
        fun getDataOrder(result: OrderDetailResponse)
        fun getErrorOrder(throwable: Throwable)
        fun showLoading()
        fun hideLoading()
    }
}
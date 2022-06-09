package com.spin.id.ui.home.profile

import com.spin.id.api.response.order.orderlist.OrderCounts
import com.spin.id.api.response.profile.EditUserNameResponse

class ProfileContract {

    interface View {
        fun showProgress()
        fun hideProgress()
        fun getData(result: EditUserNameResponse)
        fun getTotalActiveOrder(result: OrderCounts)
        fun showError(throwable: Throwable)
    }
}
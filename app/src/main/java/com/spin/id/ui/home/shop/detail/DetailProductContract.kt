package com.spin.id.ui.home.shop.detail

import com.spin.id.api.response.shop.buy.BuyResponse
import com.spin.id.api.response.shop.detail.DetailProductResponse
import com.spin.id.api.response.shop.discount.DiscountResponse
import com.spin.id.api.response.shop.payment.PaymentMethodResponse
import com.spin.id.api.response.validation.CheckUserResponse

class DetailProductContract {

    interface View {
        fun showProgress()
        fun hideProgress()
        fun getDataDetail(result: DetailProductResponse)
        fun getDataPaymentMethod(result: PaymentMethodResponse)
        fun getDataDiscount(result: DiscountResponse)
        fun getDataValidation(result: CheckUserResponse)
        fun getDataPayment(result: BuyResponse)
        fun showError(throwable: Throwable)
        fun showErrorPayment(throwable: Throwable)
    }
}
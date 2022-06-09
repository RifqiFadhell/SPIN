package com.spin.id.api.request.shop.discount

data class DiscountRequest(
    var total_order: String? = null,
    var coupon_code: String? = null
)
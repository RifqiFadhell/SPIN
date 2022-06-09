package com.spin.id.api.response.order.orderdetail

data class Order(
    val coupon_code: String? = "",
    val created_at: String? = "",
    val detail: List<Detail>,
    val discount: String? = "",
    val gmv: String? = "",
    val order_id: String? = "",
    val paid: Int? = 0,
    val status: String? = "",
    val flag_status: String? = ""
)
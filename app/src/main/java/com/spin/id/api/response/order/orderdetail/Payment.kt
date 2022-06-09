package com.spin.id.api.response.order.orderdetail

data class Payment(
    val status: String? = "",
    val expiration_date: String? = "",
    val channel_code: String? = "",
    val channel_category: String? = "",
    val account_number: String? = "",
    val logo: String? = ""
)
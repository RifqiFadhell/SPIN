package com.spin.id.api.request.order

data class OrderRequest(
    var user_id: String? = ""
)

data class DetailOrderRequest(
    var order_id: String? = ""
)
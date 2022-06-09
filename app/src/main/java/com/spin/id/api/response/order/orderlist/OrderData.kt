package com.spin.id.api.response.order.orderlist

data class OrderData(
    val coupon_code: String? = "",
    val created_at: String? = "",
    val customer_id: String? = "",
    val discount: String? = "",
    val discount_type: String? = "",
    val gmv: String? = "",
    val id: String? = "",
    val merchant_id: String? = "",
    val metadata: Metadata,
    val order_id: String? = "",
    val paid: String? = "",
    val product: OrderProduct,
    val product_price_id: String? = "",
    val status: String? = "",
    val flag_status: String? = "",
    val updated_at: String? = ""
)
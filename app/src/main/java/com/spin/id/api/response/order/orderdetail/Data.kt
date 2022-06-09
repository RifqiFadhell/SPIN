package com.spin.id.api.response.order.orderdetail

data class Data(
    val merchant: Merchant,
    val order: Order,
    val `package`: Package,
    val payment: Payment,
    val product: Product,
    val contact_link: String? = ""
)
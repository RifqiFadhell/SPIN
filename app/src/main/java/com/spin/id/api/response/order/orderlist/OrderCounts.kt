package com.spin.id.api.response.order.orderlist

data class OrderCounts(
    val cancelled_orders: Int = 0,
    val completed_orders: Int = 0,
    val process_orders: Int = 0,
    val unpaid_orders: Int = 0,
    val waiting_orders: Int = 0
)
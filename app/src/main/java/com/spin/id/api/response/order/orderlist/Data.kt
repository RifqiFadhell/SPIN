package com.spin.id.api.response.order.orderlist

data class Data(
    val counts: OrderCounts,
    val orders: List<OrderData>
)
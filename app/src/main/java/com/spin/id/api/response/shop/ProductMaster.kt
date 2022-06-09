package com.spin.id.api.response.shop

data class ProductMaster(
    val id: Int,
    val product_name: String,
    val product_price: Int? = null,
    val product_price_id: Int
)
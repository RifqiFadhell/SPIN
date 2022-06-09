package com.spin.id.api.request.shop.detail

data class DetailProductRequest(
    var master_product: String? = null,
    var product_price_id: String? = null
)
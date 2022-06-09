package com.spin.id.api.response.shop

data class ProductMasterResponse(
    val data: List<ProductMaster>,
    val message: String,
    val status: Int
)
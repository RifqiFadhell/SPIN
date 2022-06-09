package com.spin.id.api.response.shop

data class CategoryResponse(
    val data: List<Category>,
    val message: String,
    val status: Int
)
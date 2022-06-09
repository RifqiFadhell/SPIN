package com.spin.id.api.response.shop

data class ProductCategory(
    val id: Int,
    val subcategory_name: String,
    val subcategory_img: String? = null
)
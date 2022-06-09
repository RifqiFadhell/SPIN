package com.spin.id.api.response.shop

data class Category(
    val category_name: String,
    val id: String,
    var isChoosen: Boolean = false
)
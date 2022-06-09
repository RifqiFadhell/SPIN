package com.spin.id.api.response.shop.discount

import com.google.gson.annotations.SerializedName

data class DiscountResponse(

    @field:SerializedName("data")
    val data: Data? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

data class Data(

    @field:SerializedName("total_after_discount")
    val totalAfterDiscount: Int? = null,

    @field:SerializedName("discount_amount")
    val discountAmount: Int? = null
)

package com.spin.id.api.response.impression

import com.google.gson.annotations.SerializedName

data class ImpressionResponse(

    @field:SerializedName("data")
    val data: Data? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

data class Data(
    val any: Any? = null
)

package com.spin.id.api.response.validation

import com.google.gson.annotations.SerializedName

data class CheckUserResponse(

	@field:SerializedName("data")
	val data: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

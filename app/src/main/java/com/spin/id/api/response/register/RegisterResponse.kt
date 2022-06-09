package com.spin.id.api.response.register

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("message")
	val message: String? = null
)

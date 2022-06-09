package com.spin.id.api.response.profile

import com.google.gson.annotations.SerializedName

data class EditUserNameResponse(

	@field:SerializedName("data")
	val data: List<DataItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataItem(

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)

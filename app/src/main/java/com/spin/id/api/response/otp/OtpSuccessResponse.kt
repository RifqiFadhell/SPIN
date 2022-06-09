package com.spin.id.api.response.otp

import com.google.gson.annotations.SerializedName

data class OtpSuccessResponse(

	@field:SerializedName("data")
	val data: List<DataSuccess>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataSuccess(

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("otp_confirmation")
	val otpConfirmation: Int? = null
)

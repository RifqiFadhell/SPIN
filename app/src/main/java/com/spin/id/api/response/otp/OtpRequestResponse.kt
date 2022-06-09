package com.spin.id.api.response.otp

import com.google.gson.annotations.SerializedName

data class OtpRequestResponse(

	@field:SerializedName("data")
	val data: DataRequest? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataRequest(

	@field:SerializedName("length")
	val length: Int? = null,

	@field:SerializedName("trxid")
	val transactionId: String? = null,

	@field:SerializedName("first_token")
	val firstToken: String? = null,

	@field:SerializedName("otp_timeout")
	val otpTimeout: Int? = null
)

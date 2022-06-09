package com.spin.id.api.response.shop.payment

import com.google.gson.annotations.SerializedName

data class PaymentMethodResponse(

	@field:SerializedName("data")
	val data: List<DataPaymentItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataPaymentItem(

	@field:SerializedName("VIRTUAL_ACCOUNT")
	val virtualAccount: List<PaymentItem>? = null,

	@field:SerializedName("RETAIL_OUTLET")
	val retailOutlet: List<PaymentItem>? = null,

	@field:SerializedName("EWALLET")
	val ewallet: List<PaymentItem>? = null,

	@field:SerializedName("CREDIT_CARD")
	val creditCard: List<PaymentItem>? = null,

	@field:SerializedName("QRIS")
	val qris: List<PaymentItem>? = null
)

data class PaymentItem(

	val code: Int? = 1,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("logo")
	val logo: String? = null,

	@field:SerializedName("currency")
	val currency: String? = null,

	@field:SerializedName("channel_code")
	val channelCode: String? = null,

	@field:SerializedName("business_id")
	val businessId: String? = null,

	@field:SerializedName("channel_category")
	val channelCategory: String? = null
)

package com.spin.id.api.response.shop.buy

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class BuyResponse(

    @field:SerializedName("data")
    val data: BuyData? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

@Parcelize
data class BuyData(

    @field:SerializedName("account_number")
    val accountNumber: String? = null,

    @field:SerializedName("message_content")
    val messageContent: String? = null,

    @field:SerializedName("amount")
    val amount: Int? = null,

    @field:SerializedName("merchant_code")
    val merchantCode: String? = null,

    @field:SerializedName("reference_id")
    val referenceId: String? = null,

    @field:SerializedName("message_title")
    val messageTitle: String? = null,

    @field:SerializedName("action")
    val action: String? = null,

    @field:SerializedName("channel_code")
    val channelCode: String? = null,

    @field:SerializedName("expiration_date")
    val expirationDate: String? = null,

    @field:SerializedName("order_id")
    val orderId: String? = null,

    @field:SerializedName("channel_category")
    val channelCategory: String? = null,

    @field:SerializedName("payment_logo")
    val paymentLogo: String? = null,

    @field:SerializedName("timezone")
    val timezone: String? = null
) : Parcelable {

    constructor(
        orderId: String?,
        channelCode: String?,
        expirationDate: String?,
        paymentLogo: String?,
        accountNumber: String?,
        amount: Int?
    ) : this(
        accountNumber,
        null,
        amount,
        null,
        null,
        null,
        null,
        channelCode,
        expirationDate,
        orderId,
        null,
        paymentLogo
    )
}

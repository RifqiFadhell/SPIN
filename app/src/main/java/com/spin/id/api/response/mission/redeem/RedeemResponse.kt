package com.spin.id.api.response.mission.redeem

import com.google.gson.annotations.SerializedName

data class RedeemResponse(

    @field:SerializedName("data")
    val data: DataRedeem? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

data class DataRedeem(

    @field:SerializedName("user_spin_token")
    val userSpinToken: Int? = null
)

package com.spin.id.api.request.redeem

data class RedeemRequest(
    var task: ArrayList<Task>? = null,
    var user_id: String
)
package com.spin.id.api.request.otp

data class OtpVerifyRequest(

    var trxid: String? = "",
    var code: String? = "",
    var method: String? = ""
)
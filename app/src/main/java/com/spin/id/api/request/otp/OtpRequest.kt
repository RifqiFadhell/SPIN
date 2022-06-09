package com.spin.id.api.request.otp

data class OtpRequest(

    var msisdn: String? = "",
    var retry: Int? = 0,
    var method: String? = "",
    var user_id: Int? = 0
)
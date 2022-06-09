package com.spin.id.api.request.resetPassword

data class ResetRequest(
    var user_mobile: String? = "",
    var password: String? = "",
    var verify_password: String? = ""
)
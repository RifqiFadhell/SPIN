package com.spin.id.api.request.login

data class LoginRequest(

    var username: String? = "",
    var password: String? = ""
)

data class LoginSSORequest(
    var token: String? = "",
    var email: String? = "",
    var platform: String? = "",
    var utm_source: String? = null
)
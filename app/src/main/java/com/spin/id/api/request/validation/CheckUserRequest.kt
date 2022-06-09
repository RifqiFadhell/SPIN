package com.spin.id.api.request.validation

data class CheckUserRequest(

    var mobile: String? = "",
    var username: String? = "",
    var email: String? = ""
)
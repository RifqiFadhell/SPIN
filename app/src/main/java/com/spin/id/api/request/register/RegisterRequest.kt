package com.spin.id.api.request.register

data class RegisterRequest(

    var username: String? = "",
    var first_name: String? = "",
    var last_name: String? = "",
    var email: String? = "",
    var mobile: String? = "",
    var password: String? = "",
    var games: ArrayList<String>? = null,
    var topics: ArrayList<String>? = null,
    var utm_source: String? = null
)
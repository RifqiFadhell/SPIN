package com.spin.id.api.request.profile

data class EditUserNameRequest(

    var user_id: String? = "",
    var username: String? = ""
)
package com.spin.id.api.request.preference

data class UpdatePreferenceRequest (
    var games: ArrayList<String>? = null,
    var topic: ArrayList<String>? = null,
    var user_id: String
)
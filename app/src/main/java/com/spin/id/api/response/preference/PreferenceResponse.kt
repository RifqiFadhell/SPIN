package com.spin.id.api.response.preference

data class PreferenceResponse(
    val `data`: List<PreferenceData>,
    val message: String,
    val status: Int
)
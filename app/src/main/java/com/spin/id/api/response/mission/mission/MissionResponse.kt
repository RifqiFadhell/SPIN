package com.spin.id.api.response.mission.mission

data class MissionResponse(
    val `data`: MissionData,
    val message: String,
    val status: Int
)
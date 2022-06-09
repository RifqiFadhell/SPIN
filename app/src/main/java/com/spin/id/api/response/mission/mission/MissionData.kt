package com.spin.id.api.response.mission.mission

data class MissionData(
    val task: List<Task>,
    val banner_title: String,
    val banner_caption: String,
    val user_spin_token: Int
)
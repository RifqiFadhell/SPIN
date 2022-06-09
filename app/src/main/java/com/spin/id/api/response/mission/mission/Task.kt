package com.spin.id.api.response.mission.mission

data class Task(
    val task_date: String,
    val task_id: Int,
    val task_limit: Int,
    val task_name: String,
    val task_token: Int,
    val user_task_completion: Int
)
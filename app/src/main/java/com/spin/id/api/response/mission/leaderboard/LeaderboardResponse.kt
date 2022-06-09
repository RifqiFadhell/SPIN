package com.spin.id.api.response.mission.leaderboard

data class LeaderboardResponse(
    val `data`: LeaderboardData,
    val message: String,
    val status: Int
)
package com.spin.id.api.response.mission.leaderboard

data class Rank(
    val id: Int,
    val rank: Int,
    val token: Int,
    val username: String
)
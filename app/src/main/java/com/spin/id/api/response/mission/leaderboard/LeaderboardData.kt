package com.spin.id.api.response.mission.leaderboard

data class LeaderboardData(
    val end_date: String,
    val rank: List<Rank>,
    val reset_date: String,
    val start_date: String,
    val info_text: String
)
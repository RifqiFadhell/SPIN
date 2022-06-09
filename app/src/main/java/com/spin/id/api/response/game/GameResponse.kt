package com.spin.id.api.response.game

data class GameResponse(
    val `data`: List<GameData>,
    val message: String,
    val status: Int
)
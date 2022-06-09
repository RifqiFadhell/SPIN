package com.spin.id.api.response.topic

data class TopicResponse(
    val `data`: List<TopicData>,
    val message: String,
    val status: Int
)
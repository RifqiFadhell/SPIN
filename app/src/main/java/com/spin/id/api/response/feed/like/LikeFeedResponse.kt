package com.spin.id.api.response.feed.like

data class LikeFeedResponse(
    val `data`: List<LikeFeedData>,
    val message: String,
    val status: Int,
    val total_post_likes: String
)
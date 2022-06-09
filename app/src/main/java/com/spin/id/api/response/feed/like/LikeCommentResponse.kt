package com.spin.id.api.response.feed.like

data class LikeCommentResponse (
    val `data`: List<LikeFeedData>,
    val message: String,
    val status: Int,
    val total_comment_likes: String,
    val total_post_likes: String
)
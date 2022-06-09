package com.spin.id.api.response.feed.comment

data class CommentResponse(
    val current_page: Int,
    val `data`: List<CommentData>? = null,
    val items_per_page: Int,
    val message: String,
    val status: Int,
    val total_comments: String,
    val total_items: Int,
    val total_pages: Int
)
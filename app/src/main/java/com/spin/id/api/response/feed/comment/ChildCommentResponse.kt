package com.spin.id.api.response.feed.comment

data class ChildCommentResponse(
    val current_page: Int,
    val `data`: List<ChildCommentData>? = null,
    val items_per_page: Int,
    val message: String,
    val status: Int,
    val total_comments: Int,
    val total_child_comments: Int,
    val total_items: Int,
    val total_pages: Int
)
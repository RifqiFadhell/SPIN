package com.spin.id.api.response.feed.list

data class FeedResponse(
    val `data`: List<FeedData>,
    val message: String,
    val status: Int,
    val total_pages: Int,
    val total_items: Int,
    val first_page_post_id: List<String>,
    val new_posts: Int
)
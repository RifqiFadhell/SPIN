package com.spin.id.api.response.feed.comment

data class CommentData(
    var comment: String,
    var comment_id: String,
    var comment_level: String,
    var comment_level_id: String,
    var comments_count: String? = null,
    var comment_parent_id: String,
    var created_at: String,
    var deleted_by: String? = null,
    var likes_count: String,
    var like_status: String,
    var posts_id: String,
    var revision_level: String,
    var role: String,
    var updated_at: String,
    var user_id: String,
    var username: String
)
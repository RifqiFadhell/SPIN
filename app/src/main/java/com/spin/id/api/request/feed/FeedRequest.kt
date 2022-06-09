package com.spin.id.api.request.feed

data class FeedRequest(
    var user_id: String? = null,
    var games: ArrayList<String>? = null,
    var topic: ArrayList<String>? = null,
    var current_page: Int? = null,
    var first_page_post_id: ArrayList<String>? = null
)

data class LikeFeedRequest(
    var user_id: String,
    var post_id: String
)

data class LikeCommentRequest(
    var user_id: String,
    var comment_id: String
)

data class CommentListFeedRequest(
    var post_id: String,
    var current_page: Int? = null,
    var user_id: String? = null
)

data class CommentFeedRequest(
    var user_id: String,
    var post_id: String,
    var comment: String
)

data class DeleteCommentFeedRequest(
    var user_id: String,
    var comment_id: String
)

data class ChildCommentFeedRequest(
    var user_id: String,
    var current_page: Int? = null,
    var comment_id: String
)

data class CommentOnCommentRequest(
    var user_id: String,
    var post_id: String,
    var comment_parent_id: String,
    var comment: String
)
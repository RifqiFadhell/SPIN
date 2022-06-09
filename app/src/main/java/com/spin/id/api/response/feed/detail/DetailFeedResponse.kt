package com.spin.id.api.response.feed.detail

import com.google.gson.annotations.SerializedName

data class DetailFeedResponse(

	@field:SerializedName("data")
	val data: List<DataItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class DataItem(

	@field:SerializedName("thumbnail")
	val thumbnail: String? = null,

	@field:SerializedName("publishing_status")
	val publishingStatus: String? = null,

	@field:SerializedName("publisher_name")
	val publisherName: String? = null,

	@field:SerializedName("trending_id")
	val trendingId: String? = null,

	@field:SerializedName("caption")
	val caption: String? = null,

	@field:SerializedName("clicks_count")
	val clicksCount: String? = null,

	@field:SerializedName("publisher_status")
	val publisherStatus: String? = null,

	@field:SerializedName("shares_count")
	val sharesCount: String? = null,

	@field:SerializedName("likes_count")
	val likesCount: String? = null,

	@field:SerializedName("game_name")
	val gameName: String? = null,

	@field:SerializedName("post_id")
	val postId: String? = null,

	@field:SerializedName("platform_sort_order")
	val platformSortOrder: String? = null,

	@field:SerializedName("post_url")
	val postUrl: String? = null,

	@field:SerializedName("comments_count")
	val commentsCount: String? = null,

	@field:SerializedName("topic_name")
	val topicName: String? = null,

	@field:SerializedName("platform_name")
	val platformName: String? = null,

	@field:SerializedName("publisher_sort_order")
	val publisherSortOrder: String? = null,

	@field:SerializedName("publisher_image")
	val publisherImage: Any? = null,

	@field:SerializedName("topic_id")
	val topicId: String? = null,

	@field:SerializedName("platform_image")
	val platformImage: String? = null,

	@field:SerializedName("platform_status")
	val platformStatus: String? = null,

	@field:SerializedName("game_id")
	val gameId: String? = null,

	@field:SerializedName("publishing_schedule")
	val publishingSchedule: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

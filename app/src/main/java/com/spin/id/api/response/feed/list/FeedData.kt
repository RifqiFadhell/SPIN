package com.spin.id.api.response.feed.list

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeedData(
    var caption: String? = null,
    var clicks_count: String? = null,
    var comments_count: String? = null,
    var game_id: String? = null,
    var game_name: String? = null,
    var likes_count: String? = null,
    var like_status: String? = null,
    var platform_image: String? = null,
    var platform_name: String? = null,
    var platform_sort_order: String? = null,
    var platform_status: String? = null,
    var post_id: String? = null,
    var publisher_image: String? = null,
    var publisher_name: String? = null,
    var publisher_sort_order: String? = null,
    var publisher_status: String? = null,
    var publishing_schedule: String? = null,
    var publishing_status: String? = null,
    var shares_count: String? = null,
    var status: String? = null,
    var thumbnail: String? = null,
    var topic_id: String? = null,
    var topic_name: String? = null,
    var trending_id: String? = null,
    var post_url: String? = null
) : Parcelable {
    @IgnoredOnParcel
    var viewType: Int = 0

    constructor(
        _viewType: Int
    ): this (
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    ){
        viewType = _viewType
    }
}
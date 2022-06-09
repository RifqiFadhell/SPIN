package com.spin.id.api.response.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @field:SerializedName("data")
    val data: LoginData? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

data class GamesItem(

    @field:SerializedName("game_category_id")
    val gameCategoryId: String? = null,

    val name: String? = null,
    val codename: String? = null,
    val image: String? = null,
    val sort_order: String? = null,
    val status: String? = null
)

data class TopicsItem(

    @field:SerializedName("topic_category_id")
    val topicCategoryId: String? = null,

    val name: String? = null,
    val codename: String? = null,
    val image: String? = null,
    val status: String? = null,
    val sort_order: String? = null,
    val description: String? = null
)

data class LoginData(

    @field:SerializedName("user_email")
    var userEmail: String? = null,

    @field:SerializedName("user_id")
    var userId: String? = null,

    @field:SerializedName("topics")
    var topics: List<TopicsItem>? = null,

    @field:SerializedName("user_login")
    var userLogin: String? = null,

    @field:SerializedName("role")
    var role: String? = null,

    @field:SerializedName("user_mobile")
    var userNumber: String? = null,

    @field:SerializedName("otp_confirmation")
    var otpConfirmation: Int? = null,

    @field:SerializedName("games")
    var games: List<GamesItem>? = null,

    @field:SerializedName("user_nicename")
    var userNicename: String? = null,

    @field:SerializedName("user_registered")
    var userRegistered: String? = null,

    @field:SerializedName("display_name")
    var displayName: String? = null,

    @field:SerializedName("access_token")
    var token: String? = null,

    @field:SerializedName("first_name")
    var firstName: String? = null,

    @field:SerializedName("last_name")
    var lastName: String? = null

) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}


package com.spin.id.api.response.banner

data class BannerResponse(
    val `data`: List<BannerData>,
    val message: String,
    val status: Int
)
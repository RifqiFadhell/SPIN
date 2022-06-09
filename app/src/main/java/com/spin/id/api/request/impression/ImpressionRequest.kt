package com.spin.id.api.request.impression

data class ImpressionRequest(
    var device_id: String? = null,
    var data: ArrayList<DataImpression>? = null
)
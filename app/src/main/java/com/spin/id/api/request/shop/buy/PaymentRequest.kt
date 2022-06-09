package com.spin.id.api.request.shop.buy

data class PaymentRequest(
    var user_id: String? = "",
    var device_token: String? = "",
    var merchant_id: String? = "",
    var product_price_id: String? = "",
    var total_amount: String? = "",
    var channel_code: String? = "",
    var channel_category: String? = "",
    var coupon_code: String? = "",
    var discount: String? = "",
    var discount_type: String? = "",
    var costumer_data: CustomerData?,
    var metadata: HashMap<String, String>?
)

data class CustomerData(
    var first_name: String? = "",
    var last_name: String? = "",
    var email: String? = "",
    var number: String? = ""
)


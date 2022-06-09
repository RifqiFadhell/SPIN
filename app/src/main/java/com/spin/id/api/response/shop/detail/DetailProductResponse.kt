package com.spin.id.api.response.shop.detail

import com.google.gson.annotations.SerializedName

data class DetailProductResponse(

    @field:SerializedName("data")
    val data: Data? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

data class JsonMemberPackageItem(

    @field:SerializedName("refund_title")
    val refundTitle: String? = null,

    @field:SerializedName("package_name")
    val packageName: String? = null,

    @field:SerializedName("refund_description")
    val refundDescription: String? = null,

    @field:SerializedName("package_title")
    val packageTitle: String? = null,

    @field:SerializedName("package_description")
    val packageDescription: String? = null,

    @field:SerializedName("products")
    val products: List<ProductsItem>? = null
)

data class Product(

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("category")
    val category: String? = null,

    @field:SerializedName("subcategory")
    val subcategory: String? = null,

    @field:SerializedName("product_name")
    val productName: String? = null,

    @field:SerializedName("subcategory_img")
    val subcategoryImage: String? = null,

    @field:SerializedName("currency_img")
    val currencyImage: String? = null
)

data class Data(

    @field:SerializedName("product")
    val product: Product? = null,

    @field:SerializedName("package")
    val jsonMemberPackage: List<JsonMemberPackageItem>? = null
)

data class ProductsItem(

    @field:SerializedName("price")
    val price: Int? = null,

    @field:SerializedName("product_id")
    val productId: Int? = null,

    @field:SerializedName("merchant_id")
    val merchantId: Int? = null,

    @field:SerializedName("merchant_logo")
    val merchantLogo: String? = null,

    @field:SerializedName("merchant_name")
    val merchantName: String? = null,

    @field:SerializedName("selected")
    val selected: String? = null,

    @field:SerializedName("stock")
    val stock: Int? = null
)

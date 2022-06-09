package com.spin.id.api.response.shop

import com.google.gson.annotations.SerializedName

data class ProductCategoryResponse(

	@field:SerializedName("data")
	val data: List<DataProduct>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class ConfigFormItem(

	@field:SerializedName("name")
	val name: String? = "",

	@field:SerializedName("placeholder")
	val placeholder: String? = "",

	@field:SerializedName("type")
	val type: String? = "",

	@field:SerializedName("value")
	val value: String? = "",

	@field:SerializedName("required")
	val required: Boolean? = null
)

data class DataProduct(

	@field:SerializedName("subcategory_img")
	val subcategoryImg: String? = null,

	@field:SerializedName("config_form")
	val configForm: ConfigForm? = null,

	@field:SerializedName("subcategory_name")
	val subcategoryName: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class ConfigForm(

	@field:SerializedName("caption")
	val caption: String? = null,

	@field:SerializedName("config_form")
	val configForm: List<ConfigFormItem>? = null
)

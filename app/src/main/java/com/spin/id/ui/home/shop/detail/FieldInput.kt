package com.spin.id.ui.home.shop.detail

interface FieldInput {
    var key: String
    var text: String
    var type: String
    var required: Boolean
    fun getValue(): String
    fun setHint(hint: String)
    fun setInputType(type: String)
    fun setError(value: String)
}
package com.spin.id.ui.webview.utils

fun String.formatContactNumber(): String {
    return this.replace(" ", "").replace("-", "").replace("+", "")
}
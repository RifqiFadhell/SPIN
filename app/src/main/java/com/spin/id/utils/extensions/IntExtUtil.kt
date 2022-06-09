package com.spin.id.utils.extensions

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

fun Int.rupiahFormat(): String {
    val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
    return "Rp ${formatter.format(this)}"
}
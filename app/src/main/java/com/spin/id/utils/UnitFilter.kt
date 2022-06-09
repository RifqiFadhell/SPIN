package com.spin.id.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object UnitFilter {

    fun convertToComma(value: Int?): String {
        val decimalFormat: DecimalFormat = NumberFormat.getInstance(Locale.ENGLISH) as DecimalFormat
        decimalFormat.applyPattern("#,###")
        return decimalFormat.format(value)
    }

    fun convertToRupiah(value: Int?): String {
        val decimalFormat: DecimalFormat = NumberFormat.getInstance(Locale.ENGLISH) as DecimalFormat
        return "Rp ${decimalFormat.format(value)}"
    }
}
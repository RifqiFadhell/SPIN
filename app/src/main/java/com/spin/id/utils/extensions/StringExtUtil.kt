package com.spin.id.utils.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun String.copyText(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip: ClipData = ClipData.newPlainText("label", this)
    cm.setPrimaryClip(clip)
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun String.convertDate(
    startFormat: String,
    endFormat: String
): String {
    val inputFormat = SimpleDateFormat(startFormat, Locale.getDefault())
    val outputFormat = SimpleDateFormat(endFormat, Locale.getDefault())
    val parsed: Date?
    var outputText = ""

    try {
        parsed = inputFormat.parse(this)
        outputText = outputFormat.format(parsed!!)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return outputText
}

fun String.toDate(
    format: String
): Date? {
    val inputFormat = SimpleDateFormat(format, Locale.getDefault())
    val parsed = inputFormat.parse(this)
    return parsed
}
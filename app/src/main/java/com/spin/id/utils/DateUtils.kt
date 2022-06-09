package com.spin.id.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatDate(date: String, format: String): String {
        var formattedDate = ""
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val parseDate = sdf.parse(date)
            formattedDate = SimpleDateFormat(format).format(parseDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return formattedDate
    }

    fun formatDateReverse(date: String, format: String): String {
        var formattedDate = ""
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val parseDate = sdf.parse(date)
            formattedDate = SimpleDateFormat(format).format(parseDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return formattedDate
    }

    fun formatTime(time: String, format: String): String {
        var formattedTime = ""
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val parseDate = sdf.parse(time)
            formattedTime = SimpleDateFormat(format).format(parseDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return formattedTime
    }

    fun getFormatedDateTime(
        dateStr: String,
        strReadFormat: String,
        strWriteFormat: String
    ): String {

        var formattedDate = dateStr

        val readFormat = SimpleDateFormat(strReadFormat, Locale.getDefault())
        val writeFormat = SimpleDateFormat(strWriteFormat, Locale.getDefault())

        var date: Date? = null

        try {
            date = readFormat.parse(dateStr)
        } catch (e: ParseException) {
        }

        if (date != null) {
            formattedDate = writeFormat.format(date)
        }

        return formattedDate
    }

    fun getTotalTimeHour(timeIn: String, timeOut: String) : Int {

        val simpleDateFormat = SimpleDateFormat("hh:mm a")

        val date1 = simpleDateFormat.parse(timeIn)
        val date2 = simpleDateFormat.parse(timeOut)

        var hour = 0

        if (date1 != null && date2 != null) {

            val difference = date2.time - date1.time
            val days = (difference / (1000 * 60 * 60 * 24)).toInt()
            val hours: Int =  ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
            val min : Int = ((difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours) / (1000 * 60)).toInt()
            hour = if (hours < 0) -hours else hours
        }
        return hour
    }

    fun getTotalTimeMinute(timeIn: String, timeOut: String) : Int {

        val simpleDateFormat = SimpleDateFormat("hh:mm a")

        val date1 = simpleDateFormat.parse(timeIn)
        val date2 = simpleDateFormat.parse(timeOut)

        var hour = 0
        var min = 0

        if (date1 != null && date2 != null) {

            val difference = date2.time - date1.time
            min =  (difference / (1000 * 60)).toInt()
        }
        return min
    }

    fun formatSingleTime(time: String) : String {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = parser.parse(time)
        val formatter = SimpleDateFormat("HH:mm")
        return formatter.format(date)
    }
}
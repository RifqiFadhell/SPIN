package com.spin.id.utils

import android.content.Context
import android.os.Build
import com.spin.id.R
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


object TimeUtils {

    fun getFormattedDate(): String {

        val dates: String

        dates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val answer: String = current.format(formatter)
            DateUtils.formatDate(answer, "EEE, dd MMMM yyyy")

        } else {

            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val answer: String = formatter.format(date)
            DateUtils.formatDate(answer, "EEE, dd MMMM yyyy")
        }

        return dates
    }


    fun getDate(): String {
        val date: String?

        date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val answer: String = current.format(formatter)
            answer

        } else {

            val dates = Date()
            val formatter = SimpleDateFormat("dd-MM-yyyy")
            val answer: String = formatter.format(dates)
            answer
        }
        return date
    }

    fun getFullDate(): String {
        val date: String?

        date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val answer: String = current.format(formatter)
            answer

        } else {

            val dates = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val answer: String = formatter.format(dates)
            answer
        }
        return date
    }

    fun getDateReverse(): String {
        val date: String?

        date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val answer: String = current.format(formatter)
            answer

        } else {

            val dates = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val answer: String = formatter.format(dates)
            answer
        }
        return date
    }

    fun getTime(): String {
        val timeZone = TimeZone.getTimeZone("GMT+7")
        val calendar = Calendar.getInstance(timeZone)
        return String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.format("%02d", calendar.get(Calendar.MINUTE))
    }

    fun getFullTime(): String {
        val timeZone = TimeZone.getTimeZone("GMT+7")
        val calendar = Calendar.getInstance(timeZone)
        return String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" +
                String.format("%02d", calendar.get(Calendar.MILLISECOND))
    }

    fun setTimeAgo(context: Context, timeIn: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val time = sdf.parse(timeIn)

            val diff: Long = Date().time - time.time

            val r = context.resources

            val prefix: String = r.getString(R.string.time_ago_prefix)
            val suffix: String = r.getString(R.string.time_ago_suffix)

            val seconds = Math.abs(diff) / 1000.toDouble()
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val years = days / 365

            val words: String

            words = when {
                seconds < 45 -> {
                    r.getString(R.string.time_ago_seconds)
                }
                seconds < 90 -> {
                    r.getString(R.string.time_ago_minute)
                }
                minutes < 45 -> {
                    r.getString(R.string.time_ago_minutes, Math.round(minutes))
                }
                minutes < 90 -> {
                    r.getString(R.string.time_ago_hour)
                }
                hours < 24 -> {
                    r.getString(R.string.time_ago_hours, Math.round(hours))
                }
                hours < 42 -> {
                    r.getString(R.string.time_ago_day)
                }
                days < 30 -> {
                    r.getString(R.string.time_ago_days, Math.round(days))
                }
                days < 45 -> {
                    r.getString(R.string.time_ago_month)
                }
                days < 365 -> {
                    r.getString(R.string.time_ago_months, Math.round(days / 30))
                }
                years < 1.5 -> {
                    r.getString(R.string.time_ago_year)
                }
                else -> {
                    r.getString(R.string.time_ago_years, Math.round(years))
                }
            }

            val sb = StringBuilder()

            if (prefix.length > 0) {
                sb.append(prefix).append(" ")
            }

            sb.append(words)

            if (suffix.length > 0) {
                sb.append(" ").append(suffix)
            }

            return sb.toString().trim { it <= ' ' }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
package com.spin.id.utils

object RegexFilter {

    fun filterWords(value: String): String {
        return value.replace("-", "").replace(":", "").replace(" ", "")
    }

    fun splitFirstWordStrip(value: String): String {
        val time = value.split("\\s*-\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        return time[0]
    }

    fun splitSecondWordStrip(value: String): String {
        val time = value.split("\\s*-\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        return time[1]
    }

    fun splitFirstWordsColon(value: String): String {
        val time = value.split("\\s*:\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        return time[0]
    }

    fun splitFirstWords(value: String): String {
        val time = value.split("\\s* \\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return time[0]
    }

    fun splitSecondWords(value: String): String {
        val time = value.split("\\s* \\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return time[1]
    }

    fun reverseTextStrip(string: String) = string.split("-").reversed().joinToString("-")

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isNumberValid(number: String): Boolean {
        return android.util.Patterns.PHONE.matcher(number).matches()
    }

    fun isUsernameValid(username: String): Boolean {
        val regex = "^(?=.{3,20}\$)(?![_.])(?!.*[_.]{2})[a-z0-9._]+(?<![_.])\$".toRegex()
        return username.matches(regex)
    }
}
package com.spin.id.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

class HashUtil {

    companion object{
        fun hashSH256(valueToHash : String) : String{
            val messageDigest: MessageDigest
            return try {
                messageDigest = MessageDigest.getInstance("SHA-256")
                val digest = messageDigest.digest(valueToHash.toByteArray())
                val stringBuilder = StringBuilder()
                for (aDigest in digest) {
                    stringBuilder.append(String.format("%02x", aDigest and 0xFF.toByte()))
                }
                stringBuilder.toString()
            } catch (e: NoSuchAlgorithmException) {
                ""
            }

        }
    }
}
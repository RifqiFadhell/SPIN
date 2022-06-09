package com.spin.id.utils

import android.app.Application
import android.net.wifi.WifiManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class IpAddress(var context: Application) {

    fun getLocalIpAddress(): String? {
        try {

            val wifiManager: WifiManager =
                context.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
            return ipToString(wifiManager.connectionInfo.ipAddress)
        } catch (ex: Exception) {
            Log.e("IP Address", ex.toString())
        }

        return null
    }

    private fun ipToString(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF)
    }
}
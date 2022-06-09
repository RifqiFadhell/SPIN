package com.spin.id.ui.webview.utils

import android.content.Context
import android.provider.Settings

class DeviceUtil(context: Context) {

    val deviceId: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}
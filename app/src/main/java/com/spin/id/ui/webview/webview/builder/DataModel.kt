package com.spin.id.ui.webview.webview.builder

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.spin.id.api.response.feed.detail.DataItem
import com.spin.id.api.response.feed.list.FeedData

class DataModel() : Parcelable {

    var url: String? = null
    var baseUrl: String? = null
    var title: String? = null
    var like: String? = null
    var comment: String? = null
    var token: String? = null
    var clientSecret: String? = null
    var customValue: String? = null
    var customKey: String? = null
    var layerType: Int = 0
    var cacheMode: Int = 0
    var httpMethod: Int = 0
    var javascriptEnable: Boolean = true
    var allowFileAccess: Boolean = true
    var allowContentAccess: Boolean = true
    var domStorageEnabled: Boolean = true
    var useTokenFromIntent: Boolean = false
    var isDebug = false
    var versionCode: String = ""
    var appSource: String = ""
    var errorConnectionImage: Bitmap? = null
    var errorConnectionText: String? = null
    var errorConnectionTextButton: String? = null
    var errorServerImage: Bitmap? = null
    var errorServerText: String? = null
    var errorServerTextButton: String? = null
    var isButtonRetryVisible = false
    var dataFeed : FeedData? = null

    constructor(parcel: Parcel) : this() {
        url = parcel.readString()
        baseUrl = parcel.readString()
        title = parcel.readString()
        like = parcel.readString()
        comment = parcel.readString()
        token = parcel.readString()
        clientSecret = parcel.readString()
        customValue = parcel.readString()
        customKey = parcel.readString()
        layerType = parcel.readInt()
        cacheMode = parcel.readInt()
        httpMethod = parcel.readInt()
        javascriptEnable = parcel.readByte() != 0.toByte()
        allowFileAccess = parcel.readByte() != 0.toByte()
        allowContentAccess = parcel.readByte() != 0.toByte()
        domStorageEnabled = parcel.readByte() != 0.toByte()
        useTokenFromIntent = parcel.readByte() != 0.toByte()
        isDebug = parcel.readByte() != 0.toByte()
        versionCode = parcel.readString().toString()
        appSource = parcel.readString().toString()
        errorConnectionImage = parcel.readParcelable(Bitmap::class.java.classLoader)
        errorConnectionText = parcel.readString()
        errorConnectionTextButton = parcel.readString()
        errorServerImage = parcel.readParcelable(Bitmap::class.java.classLoader)
        dataFeed = parcel.readParcelable(FeedData::class.java.classLoader)
        errorServerText = parcel.readString()
        errorServerTextButton = parcel.readString()
        isButtonRetryVisible = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(baseUrl)
        parcel.writeString(title)
        parcel.writeString(like)
        parcel.writeString(comment)
        parcel.writeString(token)
        parcel.writeString(clientSecret)
        parcel.writeString(customValue)
        parcel.writeString(customKey)
        parcel.writeInt(layerType)
        parcel.writeInt(cacheMode)
        parcel.writeInt(httpMethod)
        parcel.writeByte(if (javascriptEnable) 1 else 0)
        parcel.writeByte(if (allowFileAccess) 1 else 0)
        parcel.writeByte(if (allowContentAccess) 1 else 0)
        parcel.writeByte(if (domStorageEnabled) 1 else 0)
        parcel.writeByte(if (useTokenFromIntent) 1 else 0)
        parcel.writeByte(if (isDebug) 1 else 0)
        parcel.writeString(versionCode)
        parcel.writeString(appSource)
        parcel.writeParcelable(errorConnectionImage, flags)
        parcel.writeString(errorConnectionText)
        parcel.writeString(errorConnectionTextButton)
        parcel.writeParcelable(errorServerImage, flags)
        parcel.writeParcelable(dataFeed, flags)
        parcel.writeString(errorServerText)
        parcel.writeString(errorServerTextButton)
        parcel.writeByte(if (isButtonRetryVisible) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataModel> {
        override fun createFromParcel(parcel: Parcel): DataModel {
            return DataModel(parcel)
        }

        override fun newArray(size: Int): Array<DataModel?> {
            return arrayOfNulls(size)
        }
    }

}
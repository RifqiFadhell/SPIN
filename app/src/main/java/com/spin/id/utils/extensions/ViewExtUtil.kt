package com.spin.id.utils.extensions

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.spin.id.utils.ImageUtils.Companion.loadImages

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.toVisible() {
    this.visibility = View.VISIBLE
}

fun View.toInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.toEnable() {
    this.isEnabled = true
}

fun View.toDisable() {
    this.isEnabled = false
}

fun ImageView.loadImage(context: Context, url: String?, drawable: Int) {
  loadImages(context, this, url, drawable)
}

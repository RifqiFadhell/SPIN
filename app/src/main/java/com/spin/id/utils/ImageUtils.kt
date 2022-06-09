package com.spin.id.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.spin.id.R
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class ImageUtils {

    companion object {

        const val IMAGE_NAME = "CroppedImage"

        fun convertUriToBitmap(uri: Uri, context: Context): Bitmap {
            val imageStream = context.contentResolver.openInputStream(uri)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            return selectedImage
        }

        fun convertDrawableToBitmap(drawableRes: Int, context: Context): Bitmap {
            val drawable: Drawable =
                context.resources.getDrawable(drawableRes)
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            return bitmap
        }

        fun encodeImage(bm: Bitmap): String {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val b = baos.toByteArray()

            return Base64.encodeToString(b, Base64.DEFAULT)
        }

        fun createCropActivity(context: Context, uri: Uri): UCrop {
            val uCrop = UCrop.of(uri, Uri.fromFile(
                File(context.cacheDir, IMAGE_NAME + "-" + Calendar.getInstance().timeInMillis + ".jpg")
            ))

            uCrop.withAspectRatio(1f, 1f)
            uCrop.withMaxResultSize(400, 400)

            val options = UCrop.Options()
            options.setHideBottomControls(true)
            options.setCropFrameColor(context.resources.getColor(R.color.colorPrimary))
            options.setCropGridColor(context.resources.getColor(R.color.colorPrimary))
            options.setToolbarColor(context.resources.getColor(R.color.colorPrimary))
            options.setStatusBarColor(context.resources.getColor(R.color.colorPrimary))
            uCrop.withOptions(options)

            return uCrop
        }

        fun loadImages(
            context: Context,
            imageView: ImageView,
            url: String?,
            drawable: Int
        ) {
            Glide.with(context)
                .applyDefaultRequestOptions(RequestOptions().placeholder(drawable))
                .load(url)
                .dontAnimate()
                .into(imageView)
        }
    }
}

package com.kt.unsplashimagesearch.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL
import androidx.lifecycle.lifecycleScope

object Utils {
    fun getBitmapFromURL(src: String?): Bitmap? {
//        return try {
//            val url = URL(src)
//            val connection: HttpURLConnection = url
//                .openConnection() as HttpURLConnection
//            connection.doInput = true
//            connection.connect()
//            val input: InputStream = connection.inputStream
//            return BitmapFactory.decodeStream(input)
//        } catch (e: IOException) {
//            e.printStackTrace()
//            null
//        }
        val url = URL(src)
        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        return bitmap
    }
}
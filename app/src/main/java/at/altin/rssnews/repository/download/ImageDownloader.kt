package at.altin.rssnews.repository.download

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ImageDownloader {
    private val LOG_TAG = ImageDownloader::class.java.simpleName
    private val executor: ExecutorService = Executors.newFixedThreadPool(5)

    fun downloadImage(url : URL, callback : (URL, Bitmap?) -> Unit) {
        executor.execute {
            var urlConnection: HttpURLConnection? = null
            try {
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 5000
                val statusCode = urlConnection.responseCode
                if (statusCode != 200) {
                    Log.e(LOG_TAG,"Error downloading image from $url. Response code: $statusCode")
                    callback(url, null)
                } else {
                    val inputStream = urlConnection.inputStream
                    if (inputStream == null) {
                        Log.e(LOG_TAG,"Error downloading image from $url")
                        callback(url, null)
                    } else {
                        val original = BitmapFactory.decodeStream(inputStream)
                        val factor = original.width.toDouble() / original.height.toDouble()
                        val scaled =Bitmap.createScaledBitmap(original, 800, (800.0 / factor).toInt(), true)
                        if (scaled.byteCount > 20000000) Log.w(LOG_TAG,"Image size too large: " + scaled.byteCount)
                        callback(url, scaled)
                    }
                }
            } catch (ex: IOException) {
                Log.e(LOG_TAG,"Error downloading image from $url", ex)
                callback(url, null)
            } finally {
                urlConnection?.disconnect()
            }
        }
    }
}

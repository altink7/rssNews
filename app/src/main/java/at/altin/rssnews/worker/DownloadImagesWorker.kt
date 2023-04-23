package at.altin.rssnews.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import at.altin.rssnews.activity.CHANNEL_ID
import at.altin.rssnews.activity.DetailsActivity
import at.altin.rssnews.data.AppRoomDatabase
import at.altin.rssnews.repository.NewsListRepository
import at.altin.rssnews.repository.download.NewsDownloader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DownloadImagesWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val logTag = "DownloadWorker"
    var notificationId = 1;

    private val newsListRepository = NewsListRepository(
        AppRoomDatabase.getDatabase(appContext).newsItemDao(),
        NewsDownloader(),
        WorkManager.getInstance(appContext)
    )

    override suspend fun doWork(): Result {
        val imageUrl = inputData.getString("url") ?: ""
        val title = inputData.getString("title") ?: ""
        val author = inputData.getString("author") ?: ""
        val description = inputData.getString("description") ?: ""
        val link = inputData.getString("link") ?: ""

        //download image
        val cacheDir = applicationContext.cacheDir
        val fileName = "news$title.jpg"
        val file = File(cacheDir, fileName)

        val notificationCompat = NotificationManagerCompat.from(applicationContext)

        try {
            val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                val output = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var byteCount: Int
                while (input.read(buffer).also { byteCount = it } != -1) {
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
                output.close()
                input.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return Result.failure()
        }
            val detailIntent = Intent(applicationContext, DetailsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // add the title and the image to the intent
            detailIntent.putExtra("title", title)
            detailIntent.putExtra("url", imageUrl)
            detailIntent.putExtra("author", author)
            detailIntent.putExtra("description", description)
            detailIntent.putExtra("link", link)

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    detailIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // this second flag was missing during the lesson
                )


            //display image
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val imageView = ImageView(applicationContext)
            imageView.setImageBitmap(bitmap)
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("New Item Added")
                .setContentText(title)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                .setContentIntent(pendingIntent)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED        ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return Result.success()
            }
                notificationCompat
                    .notify(notificationId++, notification)

        //Delete all orphaned images from the cache (images references only by
        //previously deleted news items).
        val newsItems = newsListRepository.getAllNewsItems()
        val cacheFiles = cacheDir.listFiles()
        for (cacheFile in cacheFiles!!) {
            var found = true
            for (newsItem in newsItems) {
                if (cacheFile.name.contains(newsItem.title.toString())) {
                    found = true
                    break
                }else{
                    found = false
                }
            }
            if (!found) {
                cacheFile.delete()
            }
        }

        return Result.success()

    }

}
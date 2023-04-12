package at.altin.rssnews.worker

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.altin.rssnews.data.AppRoomDatabase
import at.altin.rssnews.repository.NewsListRepository
import at.altin.rssnews.repository.download.NewsDownloader


class DownloadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val logTag = "DownloadWorker"
    private var notificationId = 1;

    private val newsListRepository = NewsListRepository(
        AppRoomDatabase.getDatabase(appContext).newsItemDao(),
        NewsDownloader()
    )

    override suspend fun doWork(): Result {

        val url = inputData.getString("url")?:""
        val deleteOldItems = inputData.getBoolean("deleteOldItems", true)

        //load NewsItems
        newsListRepository.loadNewsItems(url, deleteOldItems)

        Log.e(logTag, "news items loaded")

        val notification = NotificationCompat.Builder(applicationContext, "CHANNEL_ID")
            .build()

        val notificationCompat = NotificationManagerCompat.from(applicationContext)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return Result.success()
        }

        notificationCompat.notify( notificationId++, notification)

        return Result.success()
    }

}
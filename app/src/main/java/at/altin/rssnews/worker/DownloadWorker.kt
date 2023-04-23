package at.altin.rssnews.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.altin.rssnews.R
import at.altin.rssnews.activity.CHANNEL_ID
import at.altin.rssnews.activity.MainActivity
import at.altin.rssnews.data.AppRoomDatabase
import at.altin.rssnews.repository.NewsListRepository
import at.altin.rssnews.repository.download.NewsDownloader
import java.util.UUID


const val NEWS_NOTIFICATION = "News Notification"

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

        //load NewsItems and set parameter loadVal, and delete Interval in days( which defines how old the newsItems can be)
        newsListRepository.loadNewsItems(url, deleteOldItems, true,5)

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
        //Notification for the user

        // "the inner explicit intent"
        val detailIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // add data to it
        detailIntent.putExtra("at.altin.rssnews.newsid", "${UUID.randomUUID()}")

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                detailIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // this second flag was missing during the lesson
            )

        val notificationNews = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(androidx.loader.R.drawable.notification_bg_normal_pressed)
            .setContentTitle("Downloading news")
            .setContentText("Fresh News are being downloaded")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationCompat
            .notify(notificationId++, notificationNews)

        return Result.success()
    }

}
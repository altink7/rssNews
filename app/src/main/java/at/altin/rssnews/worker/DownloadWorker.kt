package at.altin.rssnews.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.altin.rssnews.data.AppRoomDatabase
import at.altin.rssnews.repository.NewsListRepository
import at.altin.rssnews.repository.download.NewsDownloader


class DownloadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val logTag = "DownloadWorker"

    private val newsListRepository = NewsListRepository(
        AppRoomDatabase.getDatabase(appContext).newsItemDao(),
        NewsDownloader()
    )

    override suspend fun doWork(): Result {

        val url = inputData.getString("url")?:""
        val deleteOldItems = inputData.getBoolean("deleteOldItems", true)

        newsListRepository.loadNewsItems(url, deleteOldItems)

        Log.e(logTag, "doWork")

        return Result.success()
    }

}
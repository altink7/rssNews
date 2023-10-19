package at.altin.rssnews.repository

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import at.altin.rssnews.data.NewsItem
import at.altin.rssnews.data.NewsItemDao
import at.altin.rssnews.repository.download.NewsDownloader
import at.altin.rssnews.worker.DownloadImagesWorker
import java.util.Date

class NewsListRepository(
    private val newsItemDao: NewsItemDao,
    private val newsDownloader: NewsDownloader,
    private val workManager: WorkManager
) {
    private val logTag = "NewsListRepository"

    val newsItems by lazy { newsItemDao.fetchAllNewsItems() }

    suspend fun loadNewsItems(newsFeedUrl: String, deleteOldItems: Boolean, isWorker: Boolean,
                              cacheImages: Boolean, deleteIntervalInDays:Int): Boolean {

        val deleteIntervalDate = Date(System.currentTimeMillis() - deleteIntervalInDays * 24 * 60 * 60 * 1000)

        if (!isWorker) {
            //just return val if not called from worker
            return false
        }
            //if called from Worker do all the Background Work
            val value = newsDownloader.load(newsFeedUrl)
            return if (value == null) {
                true
            } else {

                if (deleteOldItems) {
                    newsItemDao.deleteAllNewsItems()
                }
                val beforeInsertion = newsItemDao.getNewsItemCount()

                //https://www.derstandard.at/rss + https://www.engadget.com/rss.xml
                //insert into news_item values (31,'test','test','test','test','test','2022-04-03','key');
                newsItemDao.addNewsItems(value)

                val afterInsertion = newsItemDao.getNewsItemCount()

                //delete all news items if publication date is older than five days
                //list of news items
                val itemsToBeDeleted: MutableList<NewsItem> = ArrayList()
                value.forEach { newsItem ->
                    if(newsItem.publicationDate < deleteIntervalDate) {
                       itemsToBeDeleted.add(newsItem)
                    }
                }
                newsItemDao.deleteOldNewsItems(itemsToBeDeleted)

                //if new and cache activated, download image & show notification
                if (cacheImages&& !deleteOldItems) {
                    for(i in beforeInsertion until afterInsertion) {
                        scheduleBackgroundWorkSingleItem(value[i])
                    }
                }else if(cacheImages){
                    for(i in 0 until afterInsertion) {
                        scheduleBackgroundWorkSingleItem(value[i])
                    }
                }

                false
            }
        }


    private fun scheduleBackgroundWorkSingleItem(newsItem: NewsItem) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<DownloadImagesWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    "url" to newsItem.imageUrl,
                    "title" to newsItem.title,
                    "publicationDate" to newsItem.publicationDate.time,
                    "link" to newsItem.link,
                    "author" to newsItem.author,
                    "description" to newsItem.description
                )
            )
            .build()

        workManager.enqueue(downloadWorkRequest)
    }

    fun getAllNewsItems(): List<NewsItem> {
        return newsItemDao.fetchAllNewsItems().value ?: emptyList()
    }
}

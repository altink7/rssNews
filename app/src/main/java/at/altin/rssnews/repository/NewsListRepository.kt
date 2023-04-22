package at.altin.rssnews.repository

import at.altin.rssnews.data.NewsItem
import at.altin.rssnews.data.NewsItemDao
import at.altin.rssnews.repository.download.NewsDownloader
import java.util.Date

class NewsListRepository(
    private val newsItemDao: NewsItemDao,
    private val newsDownloader: NewsDownloader
) {
    private val logTag = "NewsListRepository"

    val newsItems by lazy { newsItemDao.fetchAllNewsItems() }

    suspend fun loadNewsItems(newsFeedUrl: String, deleteOldItems: Boolean, isWorker: Boolean, deleteIntervalInDays:Int): Boolean {

        val deleteIntervalDate = Date(System.currentTimeMillis() - deleteIntervalInDays * 24 * 60 * 60 * 1000)

        if (!isWorker) {
            //just return val if not called from worker
            return false;
        }
            //if called from Worker do all the Background Work
            val value = newsDownloader.load(newsFeedUrl)
            return if (value == null) {
                true
            } else {


                if (deleteOldItems) {
                    newsItemDao.deleteAllNewsItems()
                }

                //https://www.derstandard.at/rss + https://www.engadget.com/rss.xml
                //insert into news_item values (31,'test','test','test','test','test','2022-04-03','key');
                newsItemDao.addNewsItems(value)

                //delete all news items if publication date is older than five days
                //list of news items
                val itemsToBeDeleted: MutableList<NewsItem> = ArrayList()

                value.forEach { newsItem ->
                    if(newsItem.publicationDate < deleteIntervalDate) {
                       itemsToBeDeleted.add(newsItem)
                    }
                }

                newsItemDao.deleteOldNewsItems(itemsToBeDeleted)
                false
            }
        }
}

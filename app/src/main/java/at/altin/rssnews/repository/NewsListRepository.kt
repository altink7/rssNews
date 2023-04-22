package at.altin.rssnews.repository

import at.altin.rssnews.data.NewsItemDao
import at.altin.rssnews.repository.download.NewsDownloader

class NewsListRepository(
    private val newsItemDao: NewsItemDao,
    private val newsDownloader: NewsDownloader
) {
    private val logTag = "NewsListRepository"

    val newsItems by lazy { newsItemDao.fetchAllNewsItems() }

    suspend fun loadNewsItems(newsFeedUrl: String, deleteOldItems: Boolean, isWorker: Boolean): Boolean {
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
                newsItemDao.deleteOldNewsItems()
                false
            }
        }
}

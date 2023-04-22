package at.altin.rssnews.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.Date

@Dao
interface NewsItemDao {
    @Query("SELECT * FROM news_item ORDER BY publicationDate DESC")
    fun fetchAllNewsItems(): LiveData<List<NewsItem>>

    @Query("SELECT * FROM news_item WHERE title = :titleFromArg")
    fun findNewsItemsByTitle(titleFromArg: String): LiveData<List<NewsItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNewsItem(newsItem: NewsItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNewsItems(newsItems: List<NewsItem>)

    @Query("DELETE FROM news_item")
    suspend fun deleteAllNewsItems()

    //delete all news items if publication date is older than five days
    @Query("DELETE FROM news_item WHERE publicationDate < :dateFromArg")
    fun deleteOldNewsItems(dateFromArg: Date = Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000))
}
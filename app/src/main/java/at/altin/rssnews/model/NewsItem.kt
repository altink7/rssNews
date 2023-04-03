package at.altin.rssnews.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import at.altin.rssnews.data.converters.StringDateConverter
import at.altin.rssnews.data.converters.StringSetConverter
import java.io.Serializable
import java.util.*

@Entity(tableName = "news_item", primaryKeys = ["identifier"])
@TypeConverters(StringSetConverter::class, StringDateConverter::class)
data class NewsItem(
    @ColumnInfo(name = "identifier")
    var identifier : String,
    @ColumnInfo(name = "title")
    var title : String? = "No title",
    @ColumnInfo(name = "link")
    var link : String? = "No link",
    @ColumnInfo(name = "description")
    var description : String? = "No description",
    @ColumnInfo(name = "imageUrl")
    var imageUrl : String? = "No image",
    @ColumnInfo(name = "author")
    var author : String? = "No author",
    @ColumnInfo(name = "publicationDate")
    var publicationDate : Date = Date(),
    @ColumnInfo(name = "keywords")
    var keywords : Set<String> = emptySet()
): Serializable

package at.altin.rssnews.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import at.altin.rssnews.data.converters.StringDateConverter
import at.altin.rssnews.data.converters.StringSetConverter

@Database(entities = [NewsItem::class], version = 2, exportSchema = false)
@TypeConverters(StringSetConverter::class, StringDateConverter::class)
abstract class AppRoomDatabase : RoomDatabase(){
    abstract fun newsItemDao(): NewsItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "newsclassic_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}
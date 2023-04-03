package at.altin.rssnews.application

import android.app.Application
import at.altin.rssnews.data.AppRoomDatabase

class NewsListApplication : Application() {
    val appRoomDatabase: AppRoomDatabase by lazy { AppRoomDatabase.getDatabase(applicationContext) }
}
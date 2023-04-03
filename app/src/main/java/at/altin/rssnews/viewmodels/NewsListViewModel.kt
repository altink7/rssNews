package at.altin.rssnews.viewmodels
import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import at.altin.rssnews.R
import at.altin.rssnews.repository.NewsListRepository
import kotlinx.coroutines.launch

class NewsListViewModel(application: Application, val newsListRepository: NewsListRepository) : AndroidViewModel(application) {
    private val _error = MutableLiveData(false)
    private val _busy = MutableLiveData(true)
    val newsItems = newsListRepository.newsItems
    init {
        reload(true)
    }

    val error : LiveData<Boolean>
        get() = _error
    val busy : LiveData<Boolean>
        get() = _busy

    private fun downloadNewsItems(newsFeedUrl: String, deleteOldItems: Boolean) {
        _error.value = false
        _busy.value = true
        viewModelScope.launch {
            val loadVal = newsListRepository.loadNewsItems(newsFeedUrl, deleteOldItems)
            if(loadVal){
                _error.value = true
            }else{
                _busy.value = false
            }
        }
    }
    private fun getUrl(): String {
        val context = getApplication<Application>().applicationContext
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(
            context.getString(R.string.settings_news_url_key),
            context.getString(R.string.settings_news_url_default))
            ?: context.getString(R.string.settings_news_url_default)
    }

    fun reload(deleteOldItems : Boolean) {
        downloadNewsItems(getUrl(), deleteOldItems)
    }
}

class NewsItemViewModelFactory(private val newsListRepository: NewsListRepository, private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsListViewModel(application,newsListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

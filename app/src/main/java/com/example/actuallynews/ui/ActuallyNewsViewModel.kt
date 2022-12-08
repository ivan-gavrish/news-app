package com.example.actuallynews.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.*
import com.example.actuallynews.ActuallyNewsApplication
import com.example.actuallynews.models.DatabaseEntry
import com.example.actuallynews.models.NewsResponse
import com.example.actuallynews.repository.ActuallyNewsRepository
import com.example.actuallynews.shared.FirebaseCollections
import com.example.actuallynews.shared.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.*
import retrofit2.Response
import java.io.IOException

class ActuallyNewsViewModel(
    private val actuallyNewsRepository: ActuallyNewsRepository,
    app: Application
) : AndroidViewModel(app) {
    private var _homeBreakingNews = MutableLiveData<Resource<NewsResponse>>()
    private var _breakingNews = MutableLiveData<Resource<NewsResponse>>()
    private var _recentNews = MutableLiveData<Resource<NewsResponse>>()
    private var _searchedNews = MutableLiveData<Resource<NewsResponse>>()
    private var homeBreakingNewsPage = 1
    private var breakingNewsPage = homeBreakingNewsPage + 1
    val homeBreakingNews: LiveData<Resource<NewsResponse>> get() = _homeBreakingNews
    val breakingNews: LiveData<Resource<NewsResponse>> get() = _breakingNews
    val recentNews: LiveData<Resource<NewsResponse>> get() = _recentNews
    val searchedNews: LiveData<Resource<NewsResponse>> get() = _searchedNews
    var userProfilePictureUrl: String? = null
    var userName: String? = null
    var userEmail: String? = null
    var favoriteArticles = MutableLiveData<List<DatabaseEntry>>()
    var bookmarkedArticles = MutableLiveData<List<DatabaseEntry>>()

//    init {
//        getHomeBreakingNews()
//    }

    /**
     * Get breaking news for the home screen for the specified [country][countryCode]
     */
    fun getHomeBreakingNews(countryCode: String = "us") {
        viewModelScope.launch {
            safeHomeBreakingNewsCall(countryCode)
        }
    }

    /**
     * Call to [ActuallyNewsRepository.getBreakingNews] function with check for the Internet access
     * @param countryCode the code of the country for which to get breaking news
     */
    private suspend fun safeHomeBreakingNewsCall(countryCode: String) {
        _homeBreakingNews.postValue(Resource.Loading())
        try {
            if (!hasInternetConnection()) {
                _homeBreakingNews.postValue(Resource.Error("No Internet connection"))
            } else {
                val response =
                    actuallyNewsRepository.getBreakingNews(countryCode, homeBreakingNewsPage)
                _homeBreakingNews.postValue(handleNewsResponse(response))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> _homeBreakingNews.postValue(Resource.Error("Network failure"))
                else -> _homeBreakingNews.postValue(Resource.Error("Conversion failure"))
            }
        }
    }

    /**
     * Get breaking news for the specified [country][countryCode]
     */
    fun getBreakingNews(countryCode: String = "us") {
        viewModelScope.launch {
            safeBreakingNewsCall(countryCode)
        }
    }

    /**
     * Call to [ActuallyNewsRepository.getBreakingNews] function with check for the Internet access
     * @param countryCode the code of the country for which to get breaking news
     */
    private suspend fun safeBreakingNewsCall(countryCode: String) {
        _breakingNews.postValue(Resource.Loading())
        try {
            if (!hasInternetConnection()) {
                _breakingNews.postValue(Resource.Error("No Internet connection"))
            } else {
                val response =
                    actuallyNewsRepository.getBreakingNews(countryCode, breakingNewsPage)
                _breakingNews.postValue(handleNewsResponse(response))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> _homeBreakingNews.postValue(Resource.Error("Network failure"))
                else -> _homeBreakingNews.postValue(Resource.Error("Conversion failure"))
            }
        }
    }

    /**
     * Return the [response state][Resource] of the network request
     */
    private fun handleNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }

        return Resource.Error(response.message())
    }

    /**
     * Get recent news
     */
    fun getRecentNews() {
        viewModelScope.launch {
            safeRecentNewsCall()
        }
    }

    /**
     * Call to [ActuallyNewsRepository.getRecentNews] function with check for the Internet access
     */
    private suspend fun safeRecentNewsCall() {
        _recentNews.postValue(Resource.Loading())
        try {
            if (!hasInternetConnection()) {
                _recentNews.postValue(Resource.Error("No Internet connection"))
            } else {
                val response = actuallyNewsRepository.getRecentNews()
                _recentNews.postValue(handleNewsResponse(response))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> _recentNews.postValue(Resource.Error("Network failure"))
                else -> _recentNews.postValue(Resource.Error("Conversion failure"))
            }
        }
    }

    /**
     * Search news for the specified [searchQuery]
     */
    fun searchNews(searchQuery: String, language: String = "en") {
        viewModelScope.launch {
            safeSearchNewsCall(searchQuery, language)
        }
    }

    /**
     * Call to [ActuallyNewsRepository.searchNews] function with check for the Internet access
     * @param searchQuery the query to search news for
     */
    private suspend fun safeSearchNewsCall(searchQuery: String, language: String = "en") {
        _searchedNews.postValue(Resource.Loading())
        try {
            if (!hasInternetConnection()) {
                _searchedNews.postValue(Resource.Error("No Internet connection"))
            } else {
                val response = actuallyNewsRepository.searchNews(searchQuery, language)
                _searchedNews.postValue(handleNewsResponse(response))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> _searchedNews.postValue(Resource.Error("Network failure"))
                else -> _searchedNews.postValue(Resource.Error("Conversion failure"))
            }
        }
    }

    /**
     * Save [article][entry] into the database [collection]
     * @return true if article saved into database
     */
    suspend fun saveArticle(
        entry: DatabaseEntry,
        collection: FirebaseCollections
    ): Boolean {
        if (checkIsArticleAlreadySaved(entry.userEmail, entry.articleUrl, collection)) {
            return false
        }

        actuallyNewsRepository.saveArticle(entry, collection)
        getSavedArticles(entry.userEmail, collection)
        return true
    }

    /**
     * Delete saved [article][articleUrl] for the [user][userEmail] from the database [collection]
     */
    fun deleteArticle(
        userEmail: String,
        articleUrl: String,
        collection: FirebaseCollections
    ) {
        viewModelScope.launch {
            actuallyNewsRepository.deleteArticle(userEmail, articleUrl, collection)
            getSavedArticles(userEmail, collection)
        }
    }

    /**
     * Get saved articles for the [user][userEmail] from the database [collection]
     */
    fun getSavedArticles(userEmail: String, collection: FirebaseCollections) {
        viewModelScope.launch {
            if (collection == FirebaseCollections.FAVORITES) {
                favoriteArticles.postValue(
                    convertList(actuallyNewsRepository.getSavedArticles(userEmail, collection))
                )
            } else {
                bookmarkedArticles.postValue(
                    convertList(actuallyNewsRepository.getSavedArticles(userEmail, collection))
                )
            }
        }
    }

    /**
     * Convert MutableList<DocumentSnapshot> into List<DatabaseEntry>
     */
    private fun convertList(documents: MutableList<DocumentSnapshot>): List<DatabaseEntry> {
        val list = mutableListOf<DatabaseEntry>()
        for (document in documents) {
            list.add(document.toObject(DatabaseEntry::class.java) ?: DatabaseEntry())
        }
        return list.reversed().toList()
    }

    /**
     * Check if the [article][articleUrl] is already saved by the [user][userEmail] in the
     * database [collection]3
     */
    private suspend fun checkIsArticleAlreadySaved(
        userEmail: String,
        articleUrl: String,
        collection: FirebaseCollections
    ): Boolean {
        return actuallyNewsRepository.checkIsArticleAlreadySaved(
            userEmail,
            articleUrl,
            collection
        ).size != 0
    }

    /**
     * Check if the user has Internet connection
     * @return true if the user connected to WiFi, Mobile or Ethernet network
     */
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<ActuallyNewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // if call to active network returns null there's no internet
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }
}
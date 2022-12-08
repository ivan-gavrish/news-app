package com.example.actuallynews.shared

class Constants {
    companion object {
        const val API_KEY = "ff6b222c6c0f44ea87e86e5cf19a46dd" // reached limit
//        const val API_KEY = "5713244305204c42a211ae29bafa0c74" // reched limit
        const val BASE_URL = "https://newsapi.org"
        const val SEARCH_NEWS_TIME_DELAY = 500L // 5ms
        const val COLLECTION_FAVORITES = "favorites"
        const val COLLECTION_BOOKMARKS = "bookmarks"
        const val SHARED_PREFERENCES_NAME = "actually_news_shared_pref"
        const val SHARED_PREFERENCES_IS_GUEST = "isGuest"
    }
}
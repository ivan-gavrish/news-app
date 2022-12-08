package com.example.actuallynews.repository

import com.example.actuallynews.api.NewsApi
import com.example.actuallynews.api.RetrofitInstance
import com.example.actuallynews.models.DatabaseEntry
import com.example.actuallynews.models.NewsResponse
import com.example.actuallynews.shared.Constants.Companion.COLLECTION_FAVORITES
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import com.example.actuallynews.shared.FirebaseCollections
import com.example.actuallynews.shared.Constants.Companion.COLLECTION_BOOKMARKS
import com.google.firebase.firestore.FirebaseFirestore

class ActuallyNewsRepository(
    private val newsApi: NewsApi,
    private val firestore: FirebaseFirestore
) {
    /**
     * Get breaking news response from NewsApi with breaking news in the country
     * @param countryCode the code of the country for which to get news
     * @param pageNumber the number of page from news response
     * @return response of the request
     */
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int): Response<NewsResponse> =
        newsApi.getBreakingNews(countryCode, pageNumber)

    /**
     * Get recent news response from NewsApi with all recent news
     * @return response of the request
     */
    suspend fun getRecentNews(): Response<NewsResponse> = newsApi.getRecentNews()

    /** Get search news response from NewsApi with all found news
     * @param searchQuery the query for searching news
     * @return response of the request
     */
    suspend fun searchNews(searchQuery: String, language: String = "en"): Response<NewsResponse> =
        newsApi.searchNews(searchQuery, language = language)

    private fun getCollectionPath(collection: FirebaseCollections) =
        if (collection == FirebaseCollections.FAVORITES) {
            COLLECTION_FAVORITES
        } else {
            COLLECTION_BOOKMARKS
        }

    /**
     * Add entry to the collection
     * @param entry the entry instance to save
     * @param collection the collection's name
     */
    suspend fun saveArticle(entry: DatabaseEntry, collection: FirebaseCollections) {
        firestore.collection(getCollectionPath(collection)).add(entry).await()
    }

    /**
     * Get all liked articles for the user
     * @param userEmail the email of the requested user
     * @param collection the collection's name
     */
    suspend fun getSavedArticles(
        userEmail: String,
        collection: FirebaseCollections
    ): MutableList<DocumentSnapshot> = firestore.collection(getCollectionPath(collection))
        .whereEqualTo("userEmail", userEmail)
        .orderBy("publicationDate")
        .get()
        .await()
        .documents

    /**
     * Check if the article already saved by the user
     * @param userEmail the email of the requested user
     * @param articleUrl the url of the requested article
     * @param collection the collection's name
     */
    suspend fun checkIsArticleAlreadySaved(
        userEmail: String,
        articleUrl: String,
        collection: FirebaseCollections
    ): MutableList<DocumentSnapshot> = firestore.collection(getCollectionPath(collection))
        .whereEqualTo("userEmail", userEmail)
        .whereEqualTo("articleUrl", articleUrl)
        .get()
        .await()
        .documents

    /**
     * Delete article from the collection
     * @param userEmail the email of the requested user
     * @param articleUrl the url of the requested article
     * @param collection the collection's name
     */
    suspend fun deleteArticle(
        userEmail: String,
        articleUrl: String,
        collection: FirebaseCollections
    ) {
        firestore.collection(getCollectionPath(collection)).apply {
            document(
                this.whereEqualTo("userEmail", userEmail)
                    .whereEqualTo("articleUrl", articleUrl)
                    .get()
                    .await()
                    .documents[0].id
            ).delete().await()
        }
    }
}
package com.example.actuallynews.api

import com.example.actuallynews.models.NewsResponse
import com.example.actuallynews.shared.Constants.Companion.API_KEY
import org.intellij.lang.annotations.Language
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("/v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country") countryCode: String,
        @Query("page") pageNumber: Int,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<NewsResponse>

    @GET("/v2/everything")
    suspend fun getRecentNews(
        @Query("domains") domains: String = "abcnews.go.com,cbsnews.com,foxnews.com," +
                "news.google.com,msnbc.com,nationalreview.com,reuters.com,washingtonpost.com",
        @Query("pageSize") pageSize: Int = 100,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<NewsResponse>

    @GET("/v2/everything")
    suspend fun searchNews(
        @Query("q") searchQuery: String,
        @Query("searchIn") searchOption: String = "title",
        @Query("language") language: String = "en",
        @Query("pageSize") pageSize: Int = 100,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<NewsResponse>
}
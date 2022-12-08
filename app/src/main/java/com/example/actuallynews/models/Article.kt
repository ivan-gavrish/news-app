package com.example.actuallynews.models

import java.io.Serializable

data class Article(
    val id: Int?,
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String?,
    val source: Source?,
    val title: String?,
    val url: String,
    val urlToImage: String?
) : Serializable {
    override fun hashCode(): Int {
        var result = id.hashCode()
        if (url.isEmpty()) {
            result = 31 * result + url.hashCode()
        }
        return result
    }
}
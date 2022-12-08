package com.example.actuallynews.models

import java.io.Serializable

data class DatabaseEntry(
    val userEmail: String = "",
    val articleTitle: String = "",
    val articleUrl: String = "",
    val articleImageUrl: String = "",
    val publicationDate: String = ""
) : Serializable {
    override fun hashCode(): Int {
        var result = 7
        result += result * userEmail.hashCode() + result * articleUrl.hashCode() +
                result * articleImageUrl.hashCode()
        return result
    }
}
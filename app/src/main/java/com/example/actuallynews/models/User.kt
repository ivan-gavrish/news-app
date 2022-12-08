package com.example.actuallynews.models

import java.io.Serializable

data class User(
    val id: String?,
    val name: String?,
    val email: String?,
    val profilePictureUrl: String?
) : Serializable {
    override fun hashCode(): Int {
        var result = 7
        result += result * id.hashCode() + result * email.hashCode()
        return result
    }
}
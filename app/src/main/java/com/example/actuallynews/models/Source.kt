package com.example.actuallynews.models

import java.io.Serializable

data class Source(
    val id: String,
    val name: String
) : Serializable {
    override fun hashCode(): Int {
        var result = 7
        result += result * id.hashCode() + result * name.hashCode()
        return result
    }
}
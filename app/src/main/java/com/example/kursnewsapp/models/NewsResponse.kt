package com.example.kursnewsapp.models

data class NewsResponse(
    var articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)
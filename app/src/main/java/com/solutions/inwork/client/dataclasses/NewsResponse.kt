package com.solutions.inwork.client.dataclasses

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

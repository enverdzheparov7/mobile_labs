package com.example.movietracker

data class Movie(
    val id: Long,
    val title: String,
    val genre: String = "",
    var isWatched: Boolean = false
)

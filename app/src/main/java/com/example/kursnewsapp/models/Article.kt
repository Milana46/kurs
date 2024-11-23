package com.example.kursnewsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "article")
data class Article(
    @PrimaryKey(autoGenerate = true)
    val key:Int?=null,

    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    val source: Source,  //необходима конвертация  в TypeConverter!!!
    val title: String,
    val url: String,
    val urlToImage: String
):Serializable



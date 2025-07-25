package com.example.myapplication.model.db.entity

import androidx.room.Entity

@Entity(tableName = "notes")
data class DbNote(
    val id: Long,
    val title: String,
    val content: String,
    var isFavourite: Boolean
)
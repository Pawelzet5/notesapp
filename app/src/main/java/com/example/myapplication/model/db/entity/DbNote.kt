package com.example.myapplication.model.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class DbNote(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val remoteId: Long? = null,
    val title: String,
    val content: String,
    var isFavourite: Boolean,
    var lastModified: Long = 0L,
    var syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class SyncStatus {
    SYNCED,
    PENDING_INSERT,
    PENDING_UPDATE,
    PENDING_DELETE;
}
package com.example.myapplication.model.repository

import com.example.myapplication.model.db.entity.DbNote
import kotlinx.coroutines.flow.Flow

interface INoteRepository {

    fun getNotesFlow(showFavoritesOnly: Boolean) : Flow<List<DbNote>>

    suspend fun synchronizeNotes()

    suspend fun insertNote(title: String, content: String, isFavourite: Boolean)

    suspend fun deleteNote(dbNote: DbNote)

    suspend fun updateNote(dbNote: DbNote)

    suspend fun enqueueNotesImmediateSync()
}
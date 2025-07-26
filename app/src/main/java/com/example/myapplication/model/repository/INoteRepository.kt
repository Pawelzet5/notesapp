package com.example.myapplication.model.repository

import com.example.myapplication.model.db.entity.DbNote
import kotlinx.coroutines.flow.Flow

interface INoteRepository {
    suspend fun getAllNotesFlow(): Flow<List<DbNote>>

    suspend fun synchroniseNotes()

    suspend fun insertNote(contentInput: String, titleInput: String? = null)

    suspend fun deleteNote(dbNote: DbNote)

    suspend fun updateNote(dbNote: DbNote)
}
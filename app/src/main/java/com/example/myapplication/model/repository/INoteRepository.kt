package com.example.myapplication.model.repository

import com.example.myapplication.model.db.entity.DbNote
import kotlinx.coroutines.flow.Flow

interface INoteRepository {
    suspend fun getAllNotesFlow(): Flow<List<DbNote>>

    suspend fun synchroniseNotes()

    suspend fun insertNote(title: String, content: String)

    suspend fun deleteNote(dbNote: DbNote)

    suspend fun updateNote(dbNote: DbNote)
}
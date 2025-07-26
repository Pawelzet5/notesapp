package com.example.myapplication.model.repository

import com.example.models.database.Note
import com.example.myapplication.model.db.entity.DbNote
import kotlinx.coroutines.flow.Flow

interface INoteRepository {
    suspend fun getAllNotesFlow(): Flow<List<DbNote>>

    suspend fun insertNote(dbNote: DbNote)

    suspend fun deleteNote(dbNote: DbNote)
}
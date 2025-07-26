package com.example.myapplication.model

import com.example.ktor_client.ApiClient
import com.example.models.dto.CreateNoteBody
import com.example.myapplication.model.db.dao.NoteDao
import com.example.myapplication.model.db.entity.DbNote
import com.example.myapplication.model.repository.INoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) : INoteRepository {

    private val noteApiClient = ApiClient()

    override suspend fun getAllNotesFlow(): Flow<List<DbNote>> = noteDao.getAllNotes()


    override suspend fun insertNote(dbNote: DbNote) {
        noteDao.insertNote(dbNote)
        noteApiClient.addNote(CreateNoteBody(dbNote.content)) // TODO("Upload note's title and isFavourite flag")
    }

    override suspend fun deleteNote(dbNote: DbNote) {
        noteDao.deleteNote(dbNote)
        dbNote.remoteId?.let {
            noteApiClient.deleteNote(it)
        }
    }
}
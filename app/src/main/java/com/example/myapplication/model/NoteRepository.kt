package com.example.myapplication.model

import com.example.ktor_client.ApiClient
import com.example.myapplication.model.db.dao.NoteDao
import com.example.myapplication.model.db.entity.DbNote
import com.example.myapplication.model.repository.INoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) : INoteRepository {

    private val noteApiClient by lazy { ApiClient() }

    override suspend fun getAllNotesFlow(): Flow<List<DbNote>> = noteDao.getAllNotes()

    override suspend fun synchroniseNotes() {
        val backendNotes = noteApiClient.getNotes()
        val localNotes = noteDao.getAllNotes().last()
        val backendNotesIds = backendNotes.map { it.id }.toSet()
        val localNotesIds = localNotes.map { it.remoteId }.toSet()

        // TODO(Implement Synchronization)
    }

    override suspend fun insertNote(title: String, content: String) {
        try {
            val note = insertNoteToDatabase(title, content)
            noteApiClient.addNote(title, content).let { assignedRemoteId ->
                noteDao.updateNote(note.copy(remoteId = assignedRemoteId))
            }
        } catch (e: Exception) {
            //TODO(Implement handling exception)
        }

    }

    override suspend fun deleteNote(dbNote: DbNote) {
        noteDao.deleteNote(dbNote)
        dbNote.remoteId?.let {
            noteApiClient.deleteNote(it)
        }
    }

    override suspend fun updateNote(dbNote: DbNote) {
        noteDao.updateNote(dbNote)
        dbNote.remoteId?.let {
            noteApiClient.updateNote(it, dbNote.isFavourite)
        }
    }

    private suspend fun insertNoteToDatabase(title: String, content: String): DbNote {
        val note = DbNote(title = title, content = content, isFavourite = false)
        val id = noteDao.insertNote(note)
        return note.copy(localId = id)
    }
}
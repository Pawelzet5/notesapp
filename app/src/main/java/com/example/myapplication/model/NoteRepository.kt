package com.example.myapplication.model

import com.example.ktor_client.ApiClient
import com.example.myapplication.model.db.dao.NoteDao
import com.example.myapplication.model.db.entity.DbNote
import com.example.myapplication.model.db.entity.SyncStatus
import com.example.myapplication.model.repository.INoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) : INoteRepository {
    private val TAG = "NoteRepo"

    private val noteApiClient by lazy { ApiClient() }

    override suspend fun getAllNotesFlow(): Flow<List<DbNote>> = noteDao.getAllNotes()

    override suspend fun synchronizeNotes() {
        try {
            val allLocalNotes = noteDao.getAllNotes().first()
            val syncedLocalNotes = allLocalNotes.filter { it.syncStatus == SyncStatus.SYNCED }
            syncedLocalNotes.forEach { LogUtil.debug(it.toString()) }
            val serverNotes = noteApiClient.getNotes()
            serverNotes.forEach { LogUtil.debug(it.toString()) }
            val serverNotesById = serverNotes.associateBy { it.id }

            for (localNote in syncedLocalNotes) {
                val remoteNote = localNote.remoteId?.let { serverNotesById[it] }
                if (remoteNote != null) {
                    LogUtil.debug("comparing:\n\t$remoteNote.\n\t$localNote")
                    // LWW strategy comparison
                    if (remoteNote.lastModified > localNote.lastModified) {
                        // Newer version on remote db - updating locally
                        val updatedNote = localNote.copy(
                            title = remoteNote.title,
                            content = remoteNote.content,
                            isFavourite = remoteNote.isFavourite,
                            lastModified = remoteNote.lastModified,
                            syncStatus = SyncStatus.SYNCED
                        )
                        noteDao.updateNote(updatedNote)
                    } else if (localNote.lastModified > remoteNote.lastModified) {
                        // Newer version on local db - updating on remote
                        noteApiClient.updateNote(
                            localNote.remoteId,
                            localNote.isFavourite,
                            localNote.lastModified
                        )
                    }
                } else {
                    // Note not present on remote so it was deleted - deleting locally
                    noteDao.deleteNote(localNote)
                }
            }
            // Adding new notes from remote
            val localRemoteIds = allLocalNotes.mapNotNull { it.remoteId }.toSet()
            for (serverNote in serverNotes) {
                if (!localRemoteIds.contains(serverNote.id)) {
                    val noteToInsert = DbNote(
                        localId = 0L,
                        remoteId = serverNote.id,
                        title = serverNote.title,
                        content = serverNote.content,
                        isFavourite = serverNote.isFavourite,
                        lastModified = serverNote.lastModified,
                        syncStatus = SyncStatus.SYNCED
                    )
                    noteDao.insertNote(noteToInsert)
                }
            }
        } catch (e: IOException) {
            LogUtil.error("Problem occured during sync", TAG, e)
        }

        // Adding new notes from remote
        val localRemoteIds = allLocalNotes.mapNotNull { it.remoteId }.toSet()
        for (serverNote in serverNotes) {
            if (!localRemoteIds.contains(serverNote.id)) {
                val noteToInsert = DbNote(
                    localId = 0L,
                    remoteId = serverNote.id,
                    title = serverNote.title,
                    content = serverNote.content,
                    isFavourite = serverNote.isFavourite,
                    lastModified = serverNote.lastModified,
                    syncStatus = SyncStatus.SYNCED
                )
                noteDao.insertNote(noteToInsert)
            }
        }
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
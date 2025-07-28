package com.example.myapplication.model.repository

import android.content.Context
import androidx.work.*
import com.example.ktor_client.ApiClient
import com.example.myapplication.model.LogUtil
import com.example.myapplication.model.NoteSyncWorker
import com.example.myapplication.model.db.dao.NoteDao
import com.example.myapplication.model.db.entity.DbNote
import com.example.myapplication.model.db.entity.SyncStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApiClient: ApiClient,
    @param:ApplicationContext private val context: Context
) : INoteRepository {
    private val TAG = "NoteRepo"

    override suspend fun getAllNotesFlow(): Flow<List<DbNote>> = noteDao.getAllNotesFlow()

    override suspend fun synchronizeNotes() {
        try {
            val allLocalNotes = noteDao.getAllNotes()
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
    }

    override suspend fun insertNote(title: String, content: String) {
        val note = insertNoteToDatabase(title, content)
        try {
            noteApiClient.addNote(
                title,
                content,
                note.lastModified
            ).let { assignedRemoteId ->
                noteDao.updateNote(note.copy(remoteId = assignedRemoteId))
            }
        } catch (e: IOException) {
            updateNoteSyncStatus(note, SyncStatus.PENDING_INSERT)
            enqueueNoteSyncWork()
            LogUtil.error("Error while adding note", TAG, e)
        }
    }

    override suspend fun deleteNote(dbNote: DbNote) {
        try {
            dbNote.lastModified = System.currentTimeMillis()
            dbNote.remoteId?.let {
                noteApiClient.deleteNote(it)
                noteDao.deleteNote(dbNote)
            } ?: noteDao.deleteNote(dbNote)
        } catch (e: IOException) {
            updateNoteSyncStatus(dbNote, SyncStatus.PENDING_DELETE)
            enqueueNoteSyncWork()
            LogUtil.error("Error while deleting note", TAG, e)
        }
    }

    override suspend fun updateNote(dbNote: DbNote) {
        try {
            dbNote.lastModified = System.currentTimeMillis()
            noteDao.updateNote(dbNote)
            dbNote.remoteId?.let {
                noteApiClient.updateNote(it, dbNote.isFavourite, dbNote.lastModified)
            }
        } catch (e: IOException) {
            updateNoteSyncStatus(dbNote, SyncStatus.PENDING_UPDATE)
            enqueueNoteSyncWork()
            LogUtil.error("Error while updating note", TAG, e)
        }
    }

    private suspend fun insertNoteToDatabase(title: String, content: String): DbNote {
        val dbNote = DbNote(
            title = title,
            content = content,
            isFavourite = false,
            lastModified = System.currentTimeMillis()
        )
        val id = noteDao.insertNote(dbNote)
        return dbNote.copy(localId = id)
    }

    private suspend fun updateNoteSyncStatus(note: DbNote, status: SyncStatus) {
        if (note.syncStatus == SyncStatus.PENDING_INSERT && status == SyncStatus.PENDING_UPDATE)
            return
        noteDao.updateNote(note.copy(syncStatus = status))
    }

    private fun enqueueNoteSyncWork() {
        val workRequest = OneTimeWorkRequestBuilder<NoteSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "note_sync_work",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }
}
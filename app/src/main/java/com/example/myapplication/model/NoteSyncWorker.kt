package com.example.myapplication.model

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ktor_client.ApiClient
import com.example.myapplication.model.db.dao.NoteDao
import com.example.myapplication.model.db.entity.DbNote
import com.example.myapplication.model.db.entity.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@HiltWorker
class NoteSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted  private val noteDao: NoteDao,
    @Assisted  private val apiClient: ApiClient
) : CoroutineWorker(appContext, workerParams) {
    private val TAG = "NoteSyncWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            var shouldRetry = false
            val excludedSyncStatuses = listOf(SyncStatus.SYNCED, SyncStatus.ERROR)
            val pendingNotes: List<DbNote> = noteDao.getAllNotes()
                .filterNot { it.syncStatus in excludedSyncStatuses }

            for (note in pendingNotes) {
                try {
                    when (note.syncStatus) {
                        SyncStatus.PENDING_INSERT -> {
                            val assignedRemoteId = apiClient.addNote(
                                note.title,
                                note.content,
                                note.lastModified
                            )
                            noteDao.updateNote(
                                note.copy(
                                    remoteId = assignedRemoteId,
                                    syncStatus = SyncStatus.SYNCED,
                                    lastModified = note.lastModified
                                )
                            )
                        }

                        SyncStatus.PENDING_UPDATE -> {
                            note.remoteId?.let { remoteId ->
                                apiClient.updateNote(remoteId, note.isFavourite, note.lastModified)
                                noteDao.updateNote(note.copy(syncStatus = SyncStatus.SYNCED))
                            }
                        }

                        SyncStatus.PENDING_DELETE -> {
                            note.remoteId?.let { remoteId ->
                                apiClient.deleteNote(remoteId)
                            }
                            noteDao.deleteNote(note)
                        }

                        else -> {}
                    }
                } catch (e: IOException) {
                    shouldRetry = true
                    LogUtil.error("Error while syncing note: ${note.localId}", TAG, e)
                } catch (e: Exception) {
                    noteDao.updateNote(note.copy(syncStatus = SyncStatus.ERROR))
                    LogUtil.error("Error while syncing note: ${note.localId}", TAG, e)
                }
            }
            return@withContext if (shouldRetry) Result.retry() else Result.success()
        } catch (e: Exception) {
            LogUtil.error("Error while syncing notes", TAG, e)
            return@withContext Result.retry()
        }
    }
}

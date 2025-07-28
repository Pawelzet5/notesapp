package com.example.myapplication.model.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.model.LogUtil
import com.example.myapplication.model.repository.INoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@HiltWorker
class NoteBasicSynchronizationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val noteRepository: INoteRepository
) : CoroutineWorker(appContext, workerParams) {
    private val TAG = "NoteBasicSynchronizationWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            noteRepository.synchronizeNotes()
        } catch (e: IOException) {
            LogUtil.error("Error while syncing notes periodically", TAG, e)
            return@withContext Result.retry()
        } catch (e: Exception) {
            LogUtil.error("Error while syncing notes periodicallu", TAG, e)
            return@withContext Result.failure()
        }
        Result.success()
    }
}
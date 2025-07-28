package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.work.*
import com.example.ktor_client.ApiClient
import com.example.myapplication.model.db.dao.NoteDao
import com.example.myapplication.model.repository.INoteRepository
import com.example.myapplication.model.worker.NoteSyncWorker
import com.example.myapplication.model.worker.NoteBasicSynchronizationWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NotesApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: NoteSyncWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        enqueuePeriodicNoteSyncWork()
    }

    fun enqueuePeriodicNoteSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<NoteBasicSynchronizationWorker>(
            12,
            java.util.concurrent.TimeUnit.HOURS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "periodic_note_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
}

class NoteSyncWorkerFactory @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApiClient: ApiClient,
    private val noteRepository: INoteRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            NoteSyncWorker::class.java.name -> NoteSyncWorker(
                appContext,
                workerParameters,
                noteDao,
                noteApiClient
            )

            NoteBasicSynchronizationWorker::class.java.name -> NoteBasicSynchronizationWorker(
                appContext,
                workerParameters,
                noteRepository
            )

            else -> null
        }
    }
}
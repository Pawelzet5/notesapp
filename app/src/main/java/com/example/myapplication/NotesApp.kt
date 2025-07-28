package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.work.*
import com.example.ktor_client.ApiClient
import com.example.myapplication.model.NoteSyncWorker
import com.example.myapplication.model.db.dao.NoteDao
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
}

class NoteSyncWorkerFactory @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApiClient: ApiClient
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = NoteSyncWorker(appContext, workerParameters, noteDao, noteApiClient)
}
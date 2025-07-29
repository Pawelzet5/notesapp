package com.example.ktor_client


import com.example.models.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException

class ApiClient {

    private val urlString = "http://0.0.0.0:8080"

    private val client = HttpClient(OkHttp) { install(ContentNegotiation) { json() } }

    suspend fun getNotes(): List<GetNoteBody> {
        return client.get("$urlString/notes").body()
    }

    suspend fun addNote(
        title: String,
        content: String,
        isFavourite: Boolean,
        lastModified: Long
    ): Long {
        val createNoteBody = CreateNoteBody(title, content, isFavourite, lastModified)
        val response = client.post("$urlString/note") {
            contentType(ContentType.Application.Json)
            setBody(createNoteBody)
        }
        print(response.toString())
        if (response.status == HttpStatusCode.Created)
            return response.body<CreateNoteResponse>().id
        else
            throw IOException("Error during uploading new note")
    }

    suspend fun updateNote(noteId: Long, isFavourite: Boolean, lastModified: Long): Boolean {
        val updateNoteBody = UpdateNoteBody(noteId, isFavourite, lastModified)
        val response = client.put("$urlString/note/${updateNoteBody.id}") {
            contentType(ContentType.Application.Json)
            setBody(updateNoteBody)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun deleteNote(id: Long): Boolean {
        val response = client.delete("$urlString/note/$id")
        return response.status == HttpStatusCode.OK
    }
}

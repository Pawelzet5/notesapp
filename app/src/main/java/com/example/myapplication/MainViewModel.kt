package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ktor_client.ApiClient
import com.example.models.database.Note
import com.example.models.dto.CreateNoteBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val client = ApiClient()
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val notes = client.getNotes()
            _notes.value = notes.map { Note(it.id, it.content) }
        }
    }

    fun addNote(note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            client.addNote(CreateNoteBody(note))
            loadNotes()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            client.deleteNote(note.id)
            loadNotes()
        }
    }
}
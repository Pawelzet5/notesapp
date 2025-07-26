package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.db.entity.DbNote
import com.example.myapplication.model.repository.INoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: INoteRepository
) : ViewModel() {
    private val _notes = MutableStateFlow<List<DbNote>>(emptyList())
    val notes: StateFlow<List<DbNote>> = _notes.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.getAllNotesFlow().collect {
                _notes.value = it
            }
        }
    }

    fun addNote(contentInput: String, titleInput: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.insertNote(contentInput, titleInput)
        }
    }

    fun deleteNote(dbNote: DbNote) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(dbNote)
        }
    }
}
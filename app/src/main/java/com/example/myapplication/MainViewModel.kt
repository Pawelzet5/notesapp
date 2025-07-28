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

    private val _syncInProgress = MutableStateFlow(false)
     val syncInProgress = _syncInProgress.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.synchronizeNotes()
            noteRepository.getAllNotesFlow().collect {
                _notes.value = it
            }
        }
    }

    fun addNote(titleInput: String, contentInput: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.insertNote(titleInput, contentInput)
        }
    }

    fun deleteNote(dbNote: DbNote) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(dbNote)
        }
    }

    fun toggleNoteFavorite(dbNote: DbNote) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(dbNote.copy(isFavourite = !dbNote.isFavourite))
        }
    }

    fun onRefresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _syncInProgress.value = true
            try {
                noteRepository.synchronizeNotes()
            } finally {
                _syncInProgress.value = false
            }
        }
    }
}
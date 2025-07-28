package com.example.myapplication

import androidx.lifecycle.*
import com.example.myapplication.model.db.entity.DbNote
import com.example.myapplication.model.repository.INoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: INoteRepository
) : ViewModel(), DefaultLifecycleObserver {
    private val _showFavouritesOnly = MutableStateFlow(false)
    val showFavouritesOnly: StateFlow<Boolean> = _showFavouritesOnly

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<DbNote>> = _showFavouritesOnly
        .flatMapLatest { showFavourites ->
            noteRepository.getNotesFlow(showFavourites)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _syncInProgress = MutableStateFlow(false)
    val syncInProgress = _syncInProgress.asStateFlow()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.enqueueNotesImmediateSync()
        }
    }

    fun setShowFavouritesOnly(value: Boolean) {
        _showFavouritesOnly.value = value
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

    fun toggleNoteFavourite(dbNote: DbNote) {
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
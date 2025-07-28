package com.example.myapplication.model.db.dao

import androidx.room.*
import com.example.myapplication.model.db.entity.DbNote
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE syncStatus != 'PENDING_DELETE' ORDER BY lastModified DESC")
    fun getAllNotesFlow(): Flow<List<DbNote>>

    @Query(
    """
    SELECT * FROM notes 
    WHERE isFavourite = 1 AND syncStatus != 'PENDING_DELETE' 
    ORDER BY lastModified DESC
    """
    )
    fun getFavouriteNotesFlow(): Flow<List<DbNote>>

    @Query("SELECT * FROM notes")
    fun getAllNotes(): List<DbNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(dbNote: DbNote): Long

    @Update
    suspend fun updateNote(dbNote: DbNote)

    @Delete
    suspend fun deleteNote(dbNote: DbNote)
}
package com.example.myapplication.model.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.model.db.entity.DbNote
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<DbNote>>

    @Insert
    fun insertNote(dbNote: DbNote)

    @Delete
    fun deleteNote(id: Long)
}
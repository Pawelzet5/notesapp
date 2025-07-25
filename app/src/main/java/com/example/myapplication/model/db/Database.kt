package com.example.myapplication.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.model.db.dao.NoteDao
import com.example.myapplication.model.db.entity.DbNote

@Database(entities = [DbNote::class], version = 1, exportSchema = false)
abstract class Database: RoomDatabase() {

    abstract fun noteDao(): NoteDao
}
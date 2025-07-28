package com.example.myapplication.model.di

import com.example.myapplication.model.repository.NoteRepository
import com.example.myapplication.model.repository.INoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingModule {
    @Binds
    abstract fun bindNoteRepository(impl: NoteRepository): INoteRepository
}
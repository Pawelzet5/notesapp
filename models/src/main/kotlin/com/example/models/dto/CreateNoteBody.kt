package com.example.models.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateNoteBody(
    @SerialName("title") val title: String,
    @SerialName("content") val content: String
)
package com.example.models.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteBody(
    @SerialName("id") val id: Long,
    @SerialName("isFavourite") val isFavourite: Boolean
)
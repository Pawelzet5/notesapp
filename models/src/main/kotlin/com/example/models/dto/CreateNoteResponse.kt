package com.example.models.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateNoteResponse(
    @SerialName("id") val id: Long
)
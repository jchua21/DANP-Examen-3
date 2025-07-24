package com.jean.examen3.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String, // UUID as String
    val first_name: String,
    val last_name: String,
    val created_at: String? = null
)

@Serializable
data class UserRequest(
    val id: String, // UUID as String
    val first_name: String,
    val last_name: String
)

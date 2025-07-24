package com.jean.examen3.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DetectedContact(
    val id: String? = null, // UUID auto-generated at DB
    val detector_user_id: String, // UUID FK to users table
    val detected_user_id: String, // UUID FK to users table
    val rssi: Int, // Signal strength (int2)
    val detected_at: String, // Timestamp when detection occurred
    val created_at: String? = null // Timestamp default in DB
)

@Serializable
data class DetectedContactRequest(
    val detector_user_id: String, // UUID FK to users table
    val detected_user_id: String, // UUID FK to users table
    val rssi: Int, // Signal strength
    val detected_at: String // Timestamp when detection occurred (ISO format)
)

@Serializable
data class DetectedContactResponse(
    val id: String,
    val detector_user_id: String,
    val detected_user_id: String,
    val rssi: Int,
    val detected_at: String,
    val created_at: String
)

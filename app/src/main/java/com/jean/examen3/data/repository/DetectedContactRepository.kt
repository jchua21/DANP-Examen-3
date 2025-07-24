package com.jean.examen3.data.repository

import com.jean.examen3.data.model.DetectedContact
import com.jean.examen3.data.model.DetectedContactRequest
import com.jean.examen3.data.remote.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Count
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectedContactRepository @Inject constructor() {
    
    private val supabase = SupabaseConfig.client
    
    /**
     * Insert a new detected contact into the database
     */
    suspend fun insertDetectedContact(
        detectorUserId: String,
        detectedUserId: String,
        rssi: Int,
        detectedAt: String? = null // If null, uses current timestamp
    ): Result<DetectedContact> {
        return try {
            val timestamp = detectedAt ?: getCurrentTimestamp()
            
            val contactRequest = DetectedContactRequest(
                detector_user_id = detectorUserId,
                detected_user_id = detectedUserId,
                rssi = rssi,
                detected_at = timestamp
            )
            
            val response = supabase
                .from("detected_contacts")
                .insert(contactRequest) {
                    select()
                }
                .decodeSingle<DetectedContact>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all contacts detected by a specific user
     */
    suspend fun getContactsDetectedByUser(detectorUserId: String): Result<List<DetectedContact>> {
        return try {
            val response = supabase
                .from("detected_contacts")
                .select {
                    filter {
                        eq("detector_user_id", detectorUserId)
                    }
                    order(column = "detected_at", order = Order.DESCENDING)
                }
                .decodeList<DetectedContact>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all contacts where a specific user was detected
     */
    suspend fun getContactsWhereUserWasDetected(detectedUserId: String): Result<List<DetectedContact>> {
        return try {
            val response = supabase
                .from("detected_contacts")
                .select {
                    filter {
                        eq("detected_user_id", detectedUserId)
                    }
                    order(column = "detected_at", order = Order.DESCENDING)
                }
                .decodeList<DetectedContact>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all contacts for a specific user (both detected by and detected as)
     */
    suspend fun getAllContactsForUser(userId: String): Result<List<DetectedContact>> {
        return try {
            val response = supabase
                .from("detected_contacts")
                .select {
                    filter {
                        or {
                            eq("detector_user_id", userId)
                            eq("detected_user_id", userId)
                        }
                    }
                    order(column = "detected_at", order = Order.DESCENDING)
                }
                .decodeList<DetectedContact>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get contacts within a specific time range for a user
     */
    suspend fun getContactsInTimeRange(
        userId: String,
        startTime: String,
        endTime: String
    ): Result<List<DetectedContact>> {
        return try {
            val response = supabase
                .from("detected_contacts")
                .select {
                    filter {
                        and {
                            or {
                                eq("detector_user_id", userId)
                                eq("detected_user_id", userId)
                            }
                            gte("detected_at", startTime)
                            lte("detected_at", endTime)
                        }
                    }
                    order(column = "detected_at", order = Order.DESCENDING)
                }
                .decodeList<DetectedContact>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get contacts by RSSI threshold (stronger signal = closer contact)
     */
    suspend fun getContactsByRssiThreshold(
        userId: String,
        minRssi: Int = -70 // Typical threshold for close contact
    ): Result<List<DetectedContact>> {
        return try {
            val response = supabase
                .from("detected_contacts")
                .select {
                    filter {
                        and {
                            or {
                                eq("detector_user_id", userId)
                                eq("detected_user_id", userId)
                            }
                            gte("rssi", minRssi)
                        }
                    }
                    order(column = "detected_at", order = Order.DESCENDING)
                }
                .decodeList<DetectedContact>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a specific detected contact
     */
    suspend fun deleteDetectedContact(contactId: String): Result<Unit> {
        return try {
            supabase
                .from("detected_contacts")
                .delete {
                    filter {
                        eq("id", contactId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get contact count for a user
     */
    suspend fun getContactCount(userId: String): Result<Int> {
        return try {
            val response = supabase
                .from("detected_contacts")
                .select {
                    filter {
                        or {
                            eq("detector_user_id", userId)
                            eq("detected_user_id", userId)
                        }
                    }
                    count(Count.EXACT)
                }
            
            // Note: You might need to adjust this based on how count() returns data
            Result.success(0) // Placeholder - adjust based on actual Supabase response
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Helper function to get current timestamp in ISO format
     */
    private fun getCurrentTimestamp(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date())
    }
}

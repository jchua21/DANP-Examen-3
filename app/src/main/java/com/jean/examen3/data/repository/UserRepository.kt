package com.jean.examen3.data.repository

import com.benasher44.uuid.uuid4
import com.jean.examen3.data.local.UserDataStore
import com.jean.examen3.data.model.User
import com.jean.examen3.data.model.UserRequest
import com.jean.examen3.data.remote.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDataStore: UserDataStore
) {
    
    private val supabase = SupabaseConfig.client
    
    suspend fun registerUser(firstName: String, lastName: String): Result<User> {
        return try {
            // Generate UUID for the user
            val userId = uuid4().toString()
            
            val userRequest = UserRequest(
                id = userId,
                first_name = firstName,
                last_name = lastName
            )
            
            val response = supabase
                .from("users")
                .insert(userRequest) {
                    select()
                }
                .decodeSingle<User>()
            
            // Save user data locally
            userDataStore.saveUserData(userId, firstName, lastName)
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(id: String): Result<User> {
        return try {
            val response = supabase
                .from("users")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<User>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val response = supabase
                .from("users")
                .select()
                .decodeList<User>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // DataStore methods
    fun getUserIdFromLocal() = userDataStore.getUserId()
    fun getFirstNameFromLocal() = userDataStore.getFirstName()
    fun getLastNameFromLocal() = userDataStore.getLastName()
    fun isUserRegistered() = userDataStore.isUserRegistered()
    
    suspend fun clearLocalUserData() {
        userDataStore.clearUserData()
    }
}

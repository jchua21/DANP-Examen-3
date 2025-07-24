package com.jean.examen3.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserDataStore @Inject constructor(
    private val context: Context
) {
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val FIRST_NAME_KEY = stringPreferencesKey("first_name")
        private val LAST_NAME_KEY = stringPreferencesKey("last_name")
        private val IS_REGISTERED_KEY = stringPreferencesKey("is_registered")
    }

    suspend fun saveUserData(userId: String, firstName: String, lastName: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[FIRST_NAME_KEY] = firstName
            preferences[LAST_NAME_KEY] = lastName
            preferences[IS_REGISTERED_KEY] = "true"
        }
    }

    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    fun getFirstName(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[FIRST_NAME_KEY]
        }
    }

    fun getLastName(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_NAME_KEY]
        }
    }

    fun isUserRegistered(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_REGISTERED_KEY] == "true"
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

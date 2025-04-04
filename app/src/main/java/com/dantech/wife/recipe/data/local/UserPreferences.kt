package com.dantech.wife.recipe.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_AVATAR_KEY = stringPreferencesKey("user_avatar")
        private val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
    }
    
    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: false
    }
    
    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: ""
    }
    
    val userEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY] ?: ""
    }
    
    val userAvatar: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_AVATAR_KEY] ?: ""
    }
    
    val searchHistory: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val historyString = preferences[SEARCH_HISTORY_KEY] ?: ""
        if (historyString.isEmpty()) emptyList() else historyString.split(",")
    }
    
    suspend fun setDarkTheme(isDarkTheme: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDarkTheme
        }
    }
    
    suspend fun setUserProfile(name: String, email: String, avatarUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_AVATAR_KEY] = avatarUrl
        }
    }
    
    suspend fun addSearchQuery(query: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[SEARCH_HISTORY_KEY]?.split(",") ?: emptyList()
            val newHistory = (listOf(query) + currentHistory)
                .distinct()
                .take(10)
                .joinToString(",")
            preferences[SEARCH_HISTORY_KEY] = newHistory
        }
    }
    
    suspend fun clearSearchHistory() {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_HISTORY_KEY] = ""
        }
    }
}
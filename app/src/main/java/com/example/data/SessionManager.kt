package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_USERNAME = stringPreferencesKey("session_username")
        val KEY_LAST_ACTIVE = longPreferencesKey("session_last_active")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_AUDIO_MUTED = booleanPreferencesKey("audio_muted")

        const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000L

        /**
         * Pure function to determine if a session is still valid.
         * Sessions expire after 30 days of inactivity.
         */
        fun isSessionValid(lastActiveMillis: Long, nowMillis: Long): Boolean {
            if (lastActiveMillis <= 0L) return false
            val elapsed = nowMillis - lastActiveMillis
            return elapsed in 0..THIRTY_DAYS_MILLIS
        }
    }

    val sessionUsername: Flow<String?> = context.dataStore.data.map { prefs ->
        val username = prefs[KEY_USERNAME]
        val lastActive = prefs[KEY_LAST_ACTIVE] ?: 0L
        if (username != null && isSessionValid(lastActive, System.currentTimeMillis())) {
            username
        } else {
            null
        }
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    val isAudioMuted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUDIO_MUTED] ?: false
    }

    suspend fun saveSession(username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_LAST_ACTIVE] = System.currentTimeMillis()
        }
    }

    suspend fun touchSession() {
        context.dataStore.edit { prefs ->
            if (prefs[KEY_USERNAME] != null) {
                prefs[KEY_LAST_ACTIVE] = System.currentTimeMillis()
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_LAST_ACTIVE)
        }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = languageCode
        }
    }

    suspend fun setAudioMuted(muted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUDIO_MUTED] = muted
        }
    }

    suspend fun getInitialValidSession(): String? {
        val prefs = context.dataStore.data.first()
        val username = prefs[KEY_USERNAME]
        val lastActive = prefs[KEY_LAST_ACTIVE] ?: 0L
        return if (username != null && isSessionValid(lastActive, System.currentTimeMillis())) {
            // Refresh timestamp since user opened app
            touchSession()
            username
        } else {
            if (username != null) {
                clearSession()
            }
            null
        }
    }
}

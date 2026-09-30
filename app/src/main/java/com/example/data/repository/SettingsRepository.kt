package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stayfocus_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val QUESTIONS_PER_QUEST = intPreferencesKey("questions_per_quest")
        val DAILY_BYPASS_LIMIT = intPreferencesKey("daily_bypass_limit")
        val REST_DURATION_MINUTES = intPreferencesKey("rest_duration_minutes")
        val REST_WHITELIST = stringSetPreferencesKey("rest_whitelist")
        val IS_MASTER_LOCK_ENABLED = booleanPreferencesKey("is_master_lock_enabled")
        val REST_MODE_ACTIVE = booleanPreferencesKey("rest_mode_active")
        val REST_MODE_END_TIMESTAMP = longPreferencesKey("rest_mode_end_timestamp")
    }

    val questionsPerQuest: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUESTIONS_PER_QUEST] ?: 3
    }

    val dailyBypassLimit: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_BYPASS_LIMIT] ?: 3
    }

    val restDurationMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REST_DURATION_MINUTES] ?: 10
    }

    val restWhitelist: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REST_WHITELIST] ?: emptySet()
    }

    val isMasterLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_MASTER_LOCK_ENABLED] ?: true
    }

    val restModeActive: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val active = preferences[PreferencesKeys.REST_MODE_ACTIVE] ?: false
        val end = preferences[PreferencesKeys.REST_MODE_END_TIMESTAMP] ?: 0L
        active && System.currentTimeMillis() < end
    }

    val restModeEndTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REST_MODE_END_TIMESTAMP] ?: 0L
    }

    suspend fun setQuestionsPerQuest(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUESTIONS_PER_QUEST] = count.coerceIn(1, 10)
        }
    }

    suspend fun setDailyBypassLimit(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_BYPASS_LIMIT] = limit.coerceIn(1, 10)
        }
    }

    suspend fun setRestDurationMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REST_DURATION_MINUTES] = minutes.coerceIn(1, 60)
        }
    }

    suspend fun setRestWhitelist(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REST_WHITELIST] = packages
        }
    }

    suspend fun setMasterLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_MASTER_LOCK_ENABLED] = enabled
        }
    }

    suspend fun startRestMode(durationMinutes: Int) {
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REST_MODE_ACTIVE] = true
            preferences[PreferencesKeys.REST_MODE_END_TIMESTAMP] = endTime
        }
    }

    suspend fun stopRestMode() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REST_MODE_ACTIVE] = false
            preferences[PreferencesKeys.REST_MODE_END_TIMESTAMP] = 0L
        }
    }
}

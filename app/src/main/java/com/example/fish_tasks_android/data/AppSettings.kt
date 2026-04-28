package com.example.fish_tasks_android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val language: String = "en",
    val alertThreshold: Int = 10,
    val alertLow: Boolean = true,
    val alertMedium: Boolean = true,
    val alertHigh: Boolean = true,
    val catTop: String = "",
    val catRight: String = "",
    val catBottom: String = "",
    val catLeft: String = "",
    val score: Int = 0,
    val streak: Int = 0,
    val completed: Int = 0,
    val expired: Int = 0,
    val deleted: Int = 0
)

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("language")
        val ALERT_THRESHOLD = intPreferencesKey("alert_threshold")
        val ALERT_LOW = booleanPreferencesKey("alert_low")
        val ALERT_MEDIUM = booleanPreferencesKey("alert_medium")
        val ALERT_HIGH = booleanPreferencesKey("alert_high")
        val CAT_TOP = stringPreferencesKey("cat_top")
        val CAT_RIGHT = stringPreferencesKey("cat_right")
        val CAT_BOTTOM = stringPreferencesKey("cat_bottom")
        val CAT_LEFT = stringPreferencesKey("cat_left")
        val SCORE = intPreferencesKey("score")
        val STREAK = intPreferencesKey("streak")
        val COMPLETED = intPreferencesKey("completed")
        val EXPIRED = intPreferencesKey("expired")
        val DELETED = intPreferencesKey("deleted")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            language = preferences[PreferencesKeys.LANGUAGE] ?: "en",
            alertThreshold = preferences[PreferencesKeys.ALERT_THRESHOLD] ?: 10,
            alertLow = preferences[PreferencesKeys.ALERT_LOW] ?: true,
            alertMedium = preferences[PreferencesKeys.ALERT_MEDIUM] ?: true,
            alertHigh = preferences[PreferencesKeys.ALERT_HIGH] ?: true,
            catTop = preferences[PreferencesKeys.CAT_TOP] ?: "",
            catRight = preferences[PreferencesKeys.CAT_RIGHT] ?: "",
            catBottom = preferences[PreferencesKeys.CAT_BOTTOM] ?: "",
            catLeft = preferences[PreferencesKeys.CAT_LEFT] ?: "",
            score = preferences[PreferencesKeys.SCORE] ?: 0,
            streak = preferences[PreferencesKeys.STREAK] ?: 0,
            completed = preferences[PreferencesKeys.COMPLETED] ?: 0,
            expired = preferences[PreferencesKeys.EXPIRED] ?: 0,
            deleted = preferences[PreferencesKeys.DELETED] ?: 0
        )
    }

    suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = settings.language
            preferences[PreferencesKeys.ALERT_THRESHOLD] = settings.alertThreshold
            preferences[PreferencesKeys.ALERT_LOW] = settings.alertLow
            preferences[PreferencesKeys.ALERT_MEDIUM] = settings.alertMedium
            preferences[PreferencesKeys.ALERT_HIGH] = settings.alertHigh
            preferences[PreferencesKeys.CAT_TOP] = settings.catTop
            preferences[PreferencesKeys.CAT_RIGHT] = settings.catRight
            preferences[PreferencesKeys.CAT_BOTTOM] = settings.catBottom
            preferences[PreferencesKeys.CAT_LEFT] = settings.catLeft
        }
    }

    suspend fun updateStats(score: Int, streak: Int, completed: Int, expired: Int, deleted: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCORE] = score
            preferences[PreferencesKeys.STREAK] = streak
            preferences[PreferencesKeys.COMPLETED] = completed
            preferences[PreferencesKeys.EXPIRED] = expired
            preferences[PreferencesKeys.DELETED] = deleted
        }
    }
}

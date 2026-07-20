package com.nvmeacademy.app.data.progress

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.learningProgressDataStore by preferencesDataStore(name = "learning_progress")

private val LAST_CHAPTER_ID = intPreferencesKey("last_chapter_id")

/** Tracks which chapter the user last swiped to in the Learn deck, for Continue/Start Over. */
class ProgressStore(private val context: Context) {

    val lastChapterId: Flow<Int?> = context.learningProgressDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[LAST_CHAPTER_ID] }

    suspend fun saveLastChapter(chapterId: Int) {
        context.learningProgressDataStore.edit { it[LAST_CHAPTER_ID] = chapterId }
    }

    suspend fun clear() {
        context.learningProgressDataStore.edit { it.clear() }
    }
}

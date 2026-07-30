package com.nvmeacademy.app.data.progress

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.learningProgressDataStore by preferencesDataStore(name = "learning_progress")

private val LAST_CHAPTER_ID = intPreferencesKey("last_chapter_id")
private val NARRATION_VOICE_NAME = stringPreferencesKey("narration_voice_name")

/** Tracks which chapter the user last swiped to in the Learn deck, for Continue/Start Over. */
class ProgressStore(private val context: Context) {

    val lastChapterId: Flow<Int?> = context.learningProgressDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[LAST_CHAPTER_ID] }

    /** The TTS voice name the user picked for chapter narration, if any. */
    val narrationVoiceName: Flow<String?> = context.learningProgressDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[NARRATION_VOICE_NAME] }

    suspend fun saveLastChapter(chapterId: Int) {
        context.learningProgressDataStore.edit { it[LAST_CHAPTER_ID] = chapterId }
    }

    suspend fun saveNarrationVoice(voiceName: String) {
        context.learningProgressDataStore.edit { it[NARRATION_VOICE_NAME] = voiceName }
    }

    suspend fun clear() {
        context.learningProgressDataStore.edit { it.clear() }
    }
}

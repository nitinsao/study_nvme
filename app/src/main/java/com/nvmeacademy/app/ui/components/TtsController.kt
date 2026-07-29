package com.nvmeacademy.app.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Thin wrapper around [TextToSpeech] that auto-selects the highest quality
 * installed voice for narration, and tracks which utterance (if any) is
 * currently speaking so "Listen" buttons can flip to a stop state.
 */
class TtsController(context: Context) {
    private var engine: TextToSpeech? = null

    var speakingId by mutableStateOf<String?>(null)
        private set

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) configureBestVoice()
        }
    }

    private fun installed(voice: Voice) =
        !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)

    private fun configureBestVoice() {
        val tts = engine ?: return
        val deviceLocale = Locale.getDefault()
        val allVoices = tts.voices.orEmpty()

        val sameLanguage = allVoices.filter { installed(it) && it.locale.language == deviceLocale.language }
        val pool = sameLanguage.ifEmpty { allVoices.filter(::installed) }

        val best = pool.sortedWith(
            compareByDescending<Voice> { it.locale.country == deviceLocale.country }
                .thenByDescending { !it.isNetworkConnectionRequired }
                .thenByDescending { it.quality }
        ).firstOrNull()

        if (best != null) {
            tts.setVoice(best)
        } else {
            tts.setLanguage(Locale.US)
        }
        tts.setSpeechRate(1.0f)
        tts.setPitch(1.0f)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                speakingId = utteranceId
            }

            override fun onDone(utteranceId: String?) {
                if (speakingId == utteranceId) speakingId = null
            }

            @Deprecated("Deprecated in Java", ReplaceWith(""))
            override fun onError(utteranceId: String?) {
                if (speakingId == utteranceId) speakingId = null
            }
        })
    }

    /** Speaking the same [id] again while it's already playing stops it instead (toggle behavior). */
    fun speak(id: String, text: String) {
        val tts = engine ?: return
        if (speakingId == id) {
            stop()
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stop() {
        engine?.stop()
        speakingId = null
    }

    fun shutdown() {
        engine?.stop()
        engine?.shutdown()
        engine = null
    }
}

@Composable
fun rememberTtsController(): TtsController {
    val context = LocalContext.current
    val controller = remember { TtsController(context) }
    DisposableEffect(Unit) {
        onDispose { controller.shutdown() }
    }
    return controller
}

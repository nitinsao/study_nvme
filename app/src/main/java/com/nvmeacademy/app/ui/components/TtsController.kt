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

/** Abbreviations the spec prose uses that a TTS engine otherwise spells out letter-by-letter. */
private val SPEECH_ABBREVIATIONS: List<Pair<Regex, String>> = listOf(
    "\\bi\\.e\\.,?" to "that is",
    "\\be\\.g\\.,?" to "for example",
    "\\betc\\." to "et cetera",
    "\\bvs\\.?\\b" to "versus",
    "\\bFigs?\\.\\s*" to "Figure ",
    "&" to "and"
).map { (pattern, replacement) -> Regex(pattern, RegexOption.IGNORE_CASE) to replacement }

/**
 * Acronyms this content uses constantly that a generic TTS engine tries to
 * pronounce as a made-up word instead of spelling out. Case-sensitive and
 * whole-word, since the spec always writes them in this exact casing; the
 * "-oF" entry must precede the plain "NVMe" one so it isn't half-consumed
 * by the shorter pattern first.
 */
private val ACRONYM_PRONUNCIATIONS: List<Pair<Regex, String>> = listOf(
    "\\bNVMe-oF\\b" to "N V M e over Fabrics",
    "\\bNVMe\\b" to "N V M E",
    "\\bPCIe\\b" to "P C I E",
    "\\bSR-IOV\\b" to "S R I O V",
    "\\bRDMA\\b" to "R D M A",
    "\\bSQID\\b" to "S Q I D",
    "\\bCQID\\b" to "C Q I D",
    "\\bSQE\\b" to "S Q E",
    "\\bCQE\\b" to "C Q E",
    "\\bSGL\\b" to "S G L",
    "\\bPRP\\b" to "P R P",
    "\\bLBA\\b" to "L B A",
    "\\bSSD\\b" to "S S D",
    "\\bANA\\b" to "A N A",
    "\\bCID\\b" to "C I D",
    "\\bDIF\\b" to "D I F",
    "\\bDIX\\b" to "D I X",
    "\\bTCP\\b" to "T C P",
    "\\bSCT\\b" to "S C T"
).map { (pattern, replacement) -> Regex(pattern) to replacement }

/** "C0h-FFh" style hex ranges: handled before HEX_VALUE so the hyphen doesn't end up glued to a bare "hex". */
private val HEX_RANGE = Regex("\\b([0-9A-F]{1,4})h-([0-9A-F]{1,4})h\\b")

/** "05h" / "0Ah" / "FFh" style hex values: spelled as a made-up word otherwise, so speak their decimal value plus "hex". */
private val HEX_VALUE = Regex("\\b([0-9A-F]{1,4})h\\b")

/**
 * "bits 31:16" / "CDW10 31:16" style bit- or byte-ranges: the spec writes
 * these high:low, but that reads naturally low-to-high, e.g. "16 till 31".
 */
private val BIT_RANGE_COLON = Regex(
    "\\b(bits?|bytes?|CDW\\d+|DW\\d+)\\s+(\\d+)\\s*:\\s*(\\d+)\\b",
    RegexOption.IGNORE_CASE
)

/** Anything else shaped like "1:1" is a ratio, read in its given order as "1 to 1". */
private val RATIO_COLON = Regex("(\\d+)\\s*:\\s*(\\d+)")

/**
 * Rewrites spec prose into something a TTS engine reads naturally: spells
 * out domain acronyms and hex values it would otherwise mangle as fake
 * words, expands common abbreviations it tends to spell out letter-by-letter,
 * distinguishes bit/byte-range colons (spoken low-to-high, "till") from
 * ratio colons (spoken in order, "to"), and demotes any other colon to a
 * comma-length pause instead of silence.
 */
internal fun normalizeForSpeech(raw: String): String {
    var text = raw
    for ((pattern, replacement) in ACRONYM_PRONUNCIATIONS) {
        text = pattern.replace(text, replacement)
    }
    text = HEX_RANGE.replace(text) { match ->
        "${match.groupValues[1].toInt(16)} hex through ${match.groupValues[2].toInt(16)} hex"
    }
    text = HEX_VALUE.replace(text) { match ->
        "${match.groupValues[1].toInt(16)} hex"
    }
    text = BIT_RANGE_COLON.replace(text) { match ->
        "${match.groupValues[1]} ${match.groupValues[3]} till ${match.groupValues[2]}"
    }
    text = RATIO_COLON.replace(text) { match ->
        "${match.groupValues[1]} to ${match.groupValues[2]}"
    }
    for ((pattern, replacement) in SPEECH_ABBREVIATIONS) {
        text = pattern.replace(text, replacement)
    }
    return text.replace(":", ",")
}

/** A human-readable quality tier for a [Voice], for a voice-picker UI. */
fun voiceQualityLabel(voice: Voice): String = when {
    voice.quality >= Voice.QUALITY_VERY_HIGH -> "Very high quality"
    voice.quality >= Voice.QUALITY_HIGH -> "High quality"
    voice.quality >= Voice.QUALITY_NORMAL -> "Normal quality"
    else -> "Low quality"
}

/** A friendly label for a [Voice]: its language/region plus a quality hint. */
fun voiceDisplayLabel(voice: Voice): String {
    val locale = voice.locale.displayName.ifBlank { voice.locale.toString() }
    val network = if (voice.isNetworkConnectionRequired) " · needs network" else ""
    return "$locale — ${voiceQualityLabel(voice)}$network"
}

/**
 * Thin wrapper around [TextToSpeech] that auto-selects the highest quality
 * installed voice for narration (or a user-chosen one), and tracks which
 * utterance (if any) is currently speaking so "Listen" buttons can flip to
 * a stop state.
 */
class TtsController(context: Context) {
    private var engine: TextToSpeech? = null
    private var ready = false
    private var pendingVoiceName: String? = null

    var speakingId by mutableStateOf<String?>(null)
        private set

    /** Every installed voice for the device's language, best quality first. */
    var availableVoices by mutableStateOf<List<Voice>>(emptyList())
        private set

    var selectedVoiceName by mutableStateOf<String?>(null)
        private set

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) onEngineReady()
        }
    }

    private fun installed(voice: Voice) =
        !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)

    private fun onEngineReady() {
        val tts = engine ?: return
        val deviceLocale = Locale.getDefault()
        val allVoices = tts.voices.orEmpty().filter(::installed)
        val sameLanguage = allVoices.filter { it.locale.language == deviceLocale.language }

        availableVoices = (sameLanguage.ifEmpty { allVoices }).sortedWith(
            compareByDescending<Voice> { it.locale.country == deviceLocale.country }
                .thenByDescending { it.quality }
        )

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

        ready = true
        val requested = pendingVoiceName
        if (requested != null && applyVoiceByName(requested)) return
        applyBestVoice()
    }

    private fun applyBestVoice() {
        val tts = engine ?: return
        val best = availableVoices.firstOrNull()
        if (best != null) {
            tts.setVoice(best)
            selectedVoiceName = best.name
        } else {
            tts.setLanguage(Locale.US)
        }
    }

    private fun applyVoiceByName(name: String): Boolean {
        val tts = engine ?: return false
        val match = availableVoices.firstOrNull { it.name == name } ?: return false
        tts.setVoice(match)
        selectedVoiceName = match.name
        return true
    }

    /** Applies a remembered voice preference (e.g. loaded from disk); falls back to auto-pick if not found. */
    fun useVoicePreference(name: String?) {
        if (!ready) {
            pendingVoiceName = name
            return
        }
        if (name == null || !applyVoiceByName(name)) applyBestVoice()
    }

    /** User-driven pick from [availableVoices], e.g. via a voice-picker dialog. */
    fun selectVoice(voice: Voice) {
        engine?.setVoice(voice)
        selectedVoiceName = voice.name
    }

    /** Speaking the same [id] again while it's already playing stops it instead (toggle behavior). */
    fun speak(id: String, text: String) {
        val tts = engine ?: return
        if (speakingId == id) {
            stop()
            return
        }
        tts.speak(normalizeForSpeech(text), TextToSpeech.QUEUE_FLUSH, null, id)
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

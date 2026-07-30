package com.nvmeacademy.app.ui.chapter

import android.speech.tts.Voice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nvmeacademy.app.data.LocalContentRepository
import com.nvmeacademy.app.data.repository.DeckCard
import com.nvmeacademy.app.ui.components.ChapterDiagram
import com.nvmeacademy.app.ui.components.DeckPositionIndicator
import com.nvmeacademy.app.ui.components.TtsController
import com.nvmeacademy.app.ui.components.pagerCardTransform
import com.nvmeacademy.app.ui.components.rememberTtsController
import com.nvmeacademy.app.ui.components.voiceDisplayLabel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * The Learn tab's swipeable deck: every chapter across every part, in one
 * continuous sequence. Swiping left/right moves to the previous/next topic
 * (Tinder-card style); the last topic viewed is persisted so the Home
 * screen can offer Continue / Start Over.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChapterScreen(chapterId: Int, onBack: () -> Unit) {
    val repository = LocalContentRepository.current
    val deck by repository.observeDeck().collectAsStateWithLifecycle(initialValue = emptyList())

    if (deck.isEmpty()) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("") }, navigationIcon = { BackIcon(onBack) }) }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val startIndex = remember(deck) { deck.indexOfFirst { it.chapter.id == chapterId }.coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { deck.size })
    val tts = rememberTtsController()
    val scope = rememberCoroutineScope()
    var showVoicePicker by remember { mutableStateOf(false) }

    val savedVoiceName by repository.observeNarrationVoiceName().collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(tts.availableVoices, savedVoiceName) {
        tts.useVoicePreference(savedVoiceName)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                deck.getOrNull(page)?.let { repository.saveLastChapter(it.chapter.id) }
            }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { tts.stop() }
    }

    val current = deck[pagerState.currentPage]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.partTitle, maxLines = 1) },
                navigationIcon = { BackIcon(onBack) },
                actions = {
                    IconButton(onClick = { showVoicePicker = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Narration voice settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                pageSpacing = 12.dp
            ) { page ->
                DeckCardPage(
                    card = deck[page],
                    tts = tts,
                    modifier = Modifier.pagerCardTransform(pagerState, page)
                )
            }
            DeckPositionIndicator(current = current.position, total = current.total)
        }
    }

    if (showVoicePicker) {
        VoicePickerDialog(
            voices = tts.availableVoices,
            selectedName = tts.selectedVoiceName,
            onSelect = { voice ->
                tts.selectVoice(voice)
                scope.launch { repository.saveNarrationVoice(voice.name) }
                showVoicePicker = false
            },
            onDismiss = { showVoicePicker = false }
        )
    }
}

@Composable
private fun VoicePickerDialog(
    voices: List<Voice>,
    selectedName: String?,
    onSelect: (Voice) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Narration voice") },
        text = {
            if (voices.isEmpty()) {
                Text("No installed voices found yet for this language.")
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    voices.forEach { voice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(voice) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = voice.name == selectedName, onClick = { onSelect(voice) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(voiceDisplayLabel(voice), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun BackIcon(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}

@Composable
private fun DeckCardPage(card: DeckCard, tts: TtsController, modifier: Modifier = Modifier) {
    var showNotes by remember(card.slide.id) { mutableStateOf(false) }
    val bullets = remember(card.slide) { card.slide.bulletPoints.split("\n").filter { it.isNotBlank() } }

    val chapterUtteranceId = "chapter-${card.slide.id}"
    val notesUtteranceId = "notes-${card.slide.id}"
    val isSpeakingChapter = tts.speakingId == chapterUtteranceId
    val isSpeakingNotes = tts.speakingId == notesUtteranceId

    Surface(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LevelBadge(level = card.chapter.level)
                    IconButton(onClick = {
                        val text = buildString {
                            append(card.chapter.title).append(". ")
                            append(card.slide.title).append(". ")
                            append(bullets.joinToString(". "))
                        }
                        tts.speak(chapterUtteranceId, text)
                    }) {
                        Icon(
                            imageVector = if (isSpeakingChapter) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                            contentDescription = if (isSpeakingChapter) "Stop listening" else "Listen to this chapter"
                        )
                    }
                }
            }
            item {
                Text(card.chapter.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
                Text(card.slide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (card.slide.diagramCaption.isNotBlank()) {
                item {
                    ChapterDiagram(
                        caption = card.slide.diagramCaption,
                        orientation = card.slide.diagramOrientation,
                        connector = card.slide.diagramConnector,
                        stepsRaw = card.slide.diagramSteps
                    )
                }
            }
            items(bullets) { bullet ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("•  ", style = MaterialTheme.typography.bodyLarge)
                    Text(bullet, style = MaterialTheme.typography.bodyLarge)
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showNotes = !showNotes }) {
                                Text(if (showNotes) "Hide detailed notes" else "Show detailed notes")
                                Icon(
                                    imageVector = if (showNotes) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = { tts.speak(notesUtteranceId, card.slide.detailedNotes) }) {
                                Icon(
                                    imageVector = if (isSpeakingNotes) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                    contentDescription = if (isSpeakingNotes) "Stop listening" else "Listen to detailed notes"
                                )
                            }
                        }
                        AnimatedVisibility(visible = showNotes) {
                            Column {
                                Text(card.slide.detailedNotes, style = MaterialTheme.typography.bodyMedium)
                                if (card.slide.sourceCitation.isNotBlank() && card.slide.sourceCitation != "N/A") {
                                    Text(
                                        text = "Source: ${card.slide.sourceCitation}",
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelBadge(level: String) {
    val color = when (level) {
        "Beginner" -> MaterialTheme.colorScheme.secondary
        "Advanced" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Text(
            text = level,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

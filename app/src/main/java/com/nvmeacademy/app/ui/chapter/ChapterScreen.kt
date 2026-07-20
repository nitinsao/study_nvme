package com.nvmeacademy.app.ui.chapter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.nvmeacademy.app.ui.components.pagerCardTransform
import kotlinx.coroutines.flow.distinctUntilChanged

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

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                deck.getOrNull(page)?.let { repository.saveLastChapter(it.chapter.id) }
            }
    }

    val current = deck[pagerState.currentPage]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.partTitle, maxLines = 1) },
                navigationIcon = { BackIcon(onBack) }
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
                    modifier = Modifier.pagerCardTransform(pagerState, page)
                )
            }
            DeckPositionIndicator(current = current.position, total = current.total)
        }
    }
}

@Composable
private fun BackIcon(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}

@Composable
private fun DeckCardPage(card: DeckCard, modifier: Modifier = Modifier) {
    var showNotes by remember(card.slide.id) { mutableStateOf(false) }
    val bullets = remember(card.slide) { card.slide.bulletPoints.split("\n").filter { it.isNotBlank() } }

    Card(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LevelBadge(level = card.chapter.level)
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
                        TextButton(onClick = { showNotes = !showNotes }) {
                            Text(if (showNotes) "Hide detailed notes" else "Show detailed notes")
                            Icon(
                                imageVector = if (showNotes) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null
                            )
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

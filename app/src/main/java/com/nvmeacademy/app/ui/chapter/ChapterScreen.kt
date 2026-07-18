package com.nvmeacademy.app.ui.chapter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nvmeacademy.app.data.LocalContentRepository
import com.nvmeacademy.app.data.db.entities.ChapterEntity
import com.nvmeacademy.app.data.db.entities.SlideEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterScreen(chapterId: Int, onBack: () -> Unit) {
    val repository = LocalContentRepository.current
    var chapter by remember { mutableStateOf<ChapterEntity?>(null) }
    val slides by repository.observeSlides(chapterId).collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(chapterId) {
        chapter = repository.getChapter(chapterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chapter?.title ?: "", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (slides.isEmpty()) {
            return@Scaffold
        }
        val pagerState = rememberPagerState(pageCount = { slides.size })

        Column(modifier = Modifier.padding(innerPadding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                SlidePage(slide = slides[page])
            }
            PagerDots(pagerState = pagerState, count = slides.size)
        }
    }
}

@Composable
private fun SlidePage(slide: SlideEntity) {
    var showNotes by remember(slide.id) { mutableStateOf(false) }
    val bullets = remember(slide) { slide.bulletPoints.split("\n").filter { it.isNotBlank() } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(slide.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(bullets) { bullet ->
            Row(verticalAlignment = Alignment.Top) {
                Text("•  ", style = MaterialTheme.typography.bodyLarge)
                Text(bullet, style = MaterialTheme.typography.bodyLarge)
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
                            Text(slide.detailedNotes, style = MaterialTheme.typography.bodyMedium)
                            if (slide.sourceCitation.isNotBlank() && slide.sourceCitation != "N/A") {
                                Text(
                                    text = "Source: ${slide.sourceCitation}",
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

@Composable
private fun PagerDots(pagerState: PagerState, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) { index ->
            val selected = pagerState.currentPage == index
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (selected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

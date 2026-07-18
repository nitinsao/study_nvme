package com.nvmeacademy.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nvmeacademy.app.data.LocalContentRepository
import com.nvmeacademy.app.data.db.entities.ChapterEntity
import com.nvmeacademy.app.data.db.entities.PartEntity

@Composable
fun HomeScreen(onChapterClick: (Int) -> Unit) {
    val repository = LocalContentRepository.current
    val parts by repository.observeParts().collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text("NVMe Academy", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Learn the NVMe protocol from first principles to advanced spec details.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        items(parts) { part ->
            PartSection(part = part, onChapterClick = onChapterClick)
        }
    }
}

@Composable
private fun PartSection(part: PartEntity, onChapterClick: (Int) -> Unit) {
    val repository = LocalContentRepository.current
    val chapters by repository.observeChaptersByPart(part.id).collectAsStateWithLifecycle(initialValue = emptyList())

    Column {
        Text(part.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(part.subtitle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            chapters.forEach { chapter ->
                ChapterCard(chapter = chapter, onClick = { onChapterClick(chapter.id) })
            }
        }
    }
}

@Composable
private fun ChapterCard(chapter: ChapterEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(chapter.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    chapter.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            LevelBadge(level = chapter.level)
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
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = level,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

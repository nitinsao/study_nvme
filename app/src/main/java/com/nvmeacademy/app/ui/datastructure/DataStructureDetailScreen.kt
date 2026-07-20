package com.nvmeacademy.app.ui.datastructure

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nvmeacademy.app.data.LocalContentRepository
import com.nvmeacademy.app.data.db.entities.DataStructureEntity
import com.nvmeacademy.app.data.db.entities.DataStructureFieldEntity
import com.nvmeacademy.app.ui.components.DeckPositionIndicator
import com.nvmeacademy.app.ui.components.pagerCardTransform

/**
 * A swipeable deck over the full Data Structure reference. Opens focused on
 * the tapped structure; swiping left/right moves to the previous/next one.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DataStructureDetailScreen(structureId: Int, onBack: () -> Unit) {
    val repository = LocalContentRepository.current
    val structures by repository.observeDataStructures().collectAsStateWithLifecycle(initialValue = emptyList())

    if (structures.isEmpty()) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("") }, navigationIcon = { BackIcon(onBack) }) }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val startIndex = remember(structures) { structures.indexOfFirst { it.id == structureId }.coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { structures.size })
    val current = structures[pagerState.currentPage]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.name, maxLines = 1) },
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
                StructureCardPage(
                    structure = structures[page],
                    modifier = Modifier.pagerCardTransform(pagerState, page)
                )
            }
            DeckPositionIndicator(current = pagerState.currentPage + 1, total = structures.size)
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
private fun StructureCardPage(structure: DataStructureEntity, modifier: Modifier = Modifier) {
    val repository = LocalContentRepository.current
    val fields by repository.observeDataStructureFields(structure.id).collectAsStateWithLifecycle(initialValue = emptyList())

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderCard(structure) }
            item {
                Text("Byte / Bit Layout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(fields, key = { it.id }) { field -> FieldRow(field) }
            item {
                Text(
                    "Source: ${structure.sourceCitation}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(structure: DataStructureEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Badge(text = structure.category)
            Text(
                structure.summary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun Badge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FieldRow(field: DataStructureFieldEntity) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = field.range,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(field.fieldName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    field.fieldDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

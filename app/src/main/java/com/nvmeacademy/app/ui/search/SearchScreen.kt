package com.nvmeacademy.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nvmeacademy.app.data.LocalContentRepository
import com.nvmeacademy.app.data.db.entities.CommandEntity
import com.nvmeacademy.app.data.db.entities.DataStructureEntity
import com.nvmeacademy.app.data.db.entities.GlossaryEntity

@Composable
fun SearchScreen(onCommandClick: (Int) -> Unit, onStructureClick: (Int) -> Unit) {
    val repository = LocalContentRepository.current
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val commands by repository.searchCommands(query).collectAsStateWithLifecycle(initialValue = emptyList())
    val structures by repository.searchDataStructures(query).collectAsStateWithLifecycle(initialValue = emptyList())
    val glossaryTerms by repository.searchGlossary(query).collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Command Reference", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            placeholder = { Text("Search a command, opcode, or term…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Commands") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Structures") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Glossary") })
        }

        when (selectedTab) {
            0 -> CommandResultsList(commands = commands, onCommandClick = onCommandClick)
            1 -> StructureResultsList(structures = structures, onStructureClick = onStructureClick)
            else -> GlossaryResultsList(terms = glossaryTerms)
        }
    }
}

@Composable
private fun CommandResultsList(commands: List<CommandEntity>, onCommandClick: (Int) -> Unit) {
    if (commands.isEmpty()) {
        EmptyState(text = "No matching commands.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(commands, key = { it.id }) { command ->
            CommandRow(command = command, onClick = { onCommandClick(command.id) })
        }
    }
}

@Composable
private fun CommandRow(command: CommandEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OpcodeBadge(opcode = command.opcode)
            Column(modifier = Modifier.weight(1f)) {
                Text(command.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(command.summary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Text(
                    command.commandSet.name.replace("_", " "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun OpcodeBadge(opcode: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = opcode,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StructureResultsList(structures: List<DataStructureEntity>, onStructureClick: (Int) -> Unit) {
    if (structures.isEmpty()) {
        EmptyState(text = "No matching data structures.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(structures, key = { it.id }) { structure ->
            StructureRow(structure = structure, onClick = { onStructureClick(structure.id) })
        }
    }
}

@Composable
private fun StructureRow(structure: DataStructureEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OpcodeBadge(opcode = structure.category)
            Column(modifier = Modifier.weight(1f)) {
                Text(structure.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(structure.summary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            }
        }
    }
}

@Composable
private fun GlossaryResultsList(terms: List<GlossaryEntity>) {
    if (terms.isEmpty()) {
        EmptyState(text = "No matching terms.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(terms, key = { it.id }) { term ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(term.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(term.definition, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

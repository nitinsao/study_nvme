package com.nvmeacademy.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class ParsedDiagramStep(val label: String, val sublabel: String, val weight: Float)

private fun parseDiagramSteps(raw: String): List<ParsedDiagramStep> {
    if (raw.isBlank()) return emptyList()
    return raw.split("\n").filter { it.isNotBlank() }.map { line ->
        val parts = line.split("::")
        val label = parts.getOrElse(0) { "" }
        val sublabel = parts.getOrElse(1) { "" }
        val weight = parts.getOrElse(2) { "1" }.toFloatOrNull() ?: 1f
        ParsedDiagramStep(label, sublabel, weight)
    }
}

/**
 * A small, hand-drawn concept diagram: a caption plus a chain of labeled
 * boxes. When [connector] is "arrow" the boxes read as a sequence (a
 * horizontally scrollable row, or a vertical stack, joined by arrows).
 * When it's anything else, boxes are laid out proportionally to their
 * weight with no arrows - useful for a byte-range map like an SQE/CQE
 * layout. No-ops (renders nothing) if [caption] is blank.
 */
@Composable
fun ChapterDiagram(
    caption: String,
    orientation: String,
    connector: String,
    stepsRaw: String,
    modifier: Modifier = Modifier
) {
    if (caption.isBlank()) return
    val steps = remember(stepsRaw) { parseDiagramSteps(stepsRaw) }
    if (steps.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            caption,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            orientation == "V" -> Column(modifier = Modifier.fillMaxWidth()) {
                steps.forEachIndexed { index, step ->
                    DiagramBox(step, Modifier.fillMaxWidth())
                    if (connector == "arrow" && index != steps.lastIndex) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            connector == "arrow" -> Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    DiagramBox(step)
                    if (index != steps.lastIndex) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
            else -> Row(modifier = Modifier.fillMaxWidth()) {
                steps.forEach { step -> DiagramBox(step, Modifier.weight(step.weight)) }
            }
        }
    }
}

@Composable
private fun DiagramBox(step: ParsedDiagramStep, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(3.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                step.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            if (step.sublabel.isNotBlank()) {
                Text(
                    step.sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

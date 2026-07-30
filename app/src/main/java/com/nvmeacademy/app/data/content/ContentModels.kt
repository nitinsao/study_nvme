package com.nvmeacademy.app.data.content

import com.nvmeacademy.app.data.db.entities.CommandSet

/**
 * Plain author-facing content model used to hand-write the app's curriculum
 * and command reference in Kotlin. [ContentRepository] flattens these into
 * Room entities on first launch.
 */
data class PartSeed(
    val id: Int,
    val order: Int,
    val title: String,
    val subtitle: String,
    val chapters: List<ChapterSeed>
)

data class SlideSeed(
    val order: Int,
    val title: String,
    val bullets: List<String>,
    val notes: String,
    val source: String,
    val diagram: ChapterDiagramSeed? = null
)

/** One labeled box in a [ChapterDiagramSeed]. [weight] sizes it relative to its siblings (e.g. to show byte widths). */
data class DiagramStepSeed(
    val label: String,
    val sublabel: String = "",
    val weight: Float = 1f
)

/**
 * A small, hand-authored diagram rendered above a slide's bullets: a
 * caption plus an ordered chain of labeled boxes. [orientation] is "H" or
 * "V"; [connector] is "arrow" (implies sequence/flow) or "none" (implies a
 * flat grouping, e.g. sibling concepts or a byte-range map).
 */
data class ChapterDiagramSeed(
    val caption: String,
    val orientation: String = "H",
    val connector: String = "arrow",
    val steps: List<DiagramStepSeed>
)

data class ChapterSeed(
    val id: Int,
    val partId: Int,
    val order: Int,
    val title: String,
    val shortDescription: String,
    val level: String,
    val slides: List<SlideSeed>
)

data class FieldSeed(
    val name: String,
    val description: String
)

data class CommandSeed(
    val id: Int,
    val opcode: String,
    val name: String,
    val commandSet: CommandSet,
    val summary: String,
    val description: String,
    val mandatory: String,
    val source: String,
    val fields: List<FieldSeed> = emptyList()
)

data class GlossarySeed(
    val term: String,
    val definition: String
)

data class StructureFieldSeed(
    val range: String,
    val fieldName: String,
    val fieldDescription: String
)

data class DataStructureSeed(
    val id: Int,
    val order: Int,
    val category: String, // "SQE", "CQE", or "Status"
    val name: String,
    val summary: String,
    val source: String,
    val fields: List<StructureFieldSeed>
)

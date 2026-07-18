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
    val source: String
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

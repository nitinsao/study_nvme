package com.nvmeacademy.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A top-level grouping of chapters, e.g. "Part 1 - Foundations". */
@Entity(tableName = "parts")
data class PartEntity(
    @PrimaryKey val id: Int,
    val order: Int,
    val title: String,
    val subtitle: String
)

/** A single chapter within a part. Rendered as a swipeable slide deck. */
@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: Int,
    val partId: Int,
    val order: Int,
    val title: String,
    val shortDescription: String,
    val level: String // "Beginner", "Intermediate", "Advanced"
)

/** One slide inside a chapter. bulletPoints are stored newline-delimited. */
@Entity(tableName = "slides")
data class SlideEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterId: Int,
    val order: Int,
    val title: String,
    val bulletPoints: String, // delimited by "\n"
    val detailedNotes: String,
    val sourceCitation: String
)

enum class CommandSet {
    ADMIN, NVM_IO, FABRICS, NVM_ADMIN_EXT, MI, MI_PCIE
}

/** A single NVMe command, searchable by name/opcode. */
@Entity(tableName = "commands")
data class CommandEntity(
    @PrimaryKey val id: Int,
    val opcode: String, // e.g. "06h", or "N/A"
    val name: String,
    val commandSet: CommandSet,
    val summary: String,
    val description: String,
    val mandatory: String, // "Mandatory", "Optional", "Conditional", "Vendor Specific"
    val sourceCitation: String
)

/** A key field/parameter of a command, shown in the command detail screen. */
@Entity(tableName = "command_fields")
data class CommandFieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commandId: Int,
    val order: Int,
    val fieldName: String,
    val fieldDescription: String
)

@Entity(tableName = "glossary")
data class GlossaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val term: String,
    val definition: String
)

/**
 * A named, byte/Dword-precise data structure from the spec (e.g. "Command Dword 0",
 * "Common Command Format", "Completion Queue Entry: DW 3") - distinct from the
 * per-command Dword10-15 usage already covered in [CommandEntity]/[CommandFieldEntity].
 */
@Entity(tableName = "data_structures")
data class DataStructureEntity(
    @PrimaryKey val id: Int,
    val order: Int,
    val category: String, // "SQE", "CQE", or "Status"
    val name: String,
    val summary: String,
    val sourceCitation: String
)

/** One row (a byte or bit range) of a [DataStructureEntity]. */
@Entity(tableName = "data_structure_fields")
data class DataStructureFieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val structureId: Int,
    val order: Int,
    val range: String, // e.g. "31:16", "07:04", "63:60"
    val fieldName: String,
    val fieldDescription: String
)

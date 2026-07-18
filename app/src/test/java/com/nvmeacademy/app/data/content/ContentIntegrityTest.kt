package com.nvmeacademy.app.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM sanity checks on the hand-authored content (chapters, slides,
 * commands, glossary). These catch data-entry mistakes - duplicate IDs,
 * blank fields, empty lists - without needing an emulator or the Room
 * database, since [ContentModels] are plain Kotlin data classes.
 */
class ContentIntegrityTest {

    @Test
    fun `part ids are unique`() {
        val ids = AllParts.parts.map { it.id }
        assertEquals("Duplicate part id found", ids.size, ids.toSet().size)
    }

    @Test
    fun `chapter ids are unique across all parts`() {
        val ids = AllParts.parts.flatMap { it.chapters }.map { it.id }
        assertEquals("Duplicate chapter id found", ids.size, ids.toSet().size)
    }

    @Test
    fun `every chapter belongs to a part that declares it and has at least one slide`() {
        for (part in AllParts.parts) {
            for (chapter in part.chapters) {
                assertEquals(
                    "Chapter ${chapter.id} (${chapter.title}) has partId ${chapter.partId} but lives under part ${part.id}",
                    part.id, chapter.partId
                )
                assertTrue("Chapter ${chapter.id} (${chapter.title}) has no slides", chapter.slides.isNotEmpty())
            }
        }
    }

    @Test
    fun `every slide has a non-blank title, at least one bullet, and notes`() {
        for (part in AllParts.parts) {
            for (chapter in part.chapters) {
                for (slide in chapter.slides) {
                    assertFalse("Blank slide title in chapter ${chapter.id}", slide.title.isBlank())
                    assertTrue("Slide '${slide.title}' in chapter ${chapter.id} has no bullets", slide.bullets.isNotEmpty())
                    assertFalse("Slide '${slide.title}' in chapter ${chapter.id} has blank notes", slide.notes.isBlank())
                }
            }
        }
    }

    @Test
    fun `command ids are unique across all command sets`() {
        val ids = AllCommands.commands.map { it.id }
        assertEquals("Duplicate command id found", ids.size, ids.toSet().size)
    }

    @Test
    fun `every command has a non-blank opcode, name, summary, description, and source`() {
        for (command in AllCommands.commands) {
            assertFalse("Command ${command.id} has blank opcode", command.opcode.isBlank())
            assertFalse("Command ${command.id} has blank name", command.name.isBlank())
            assertFalse("Command ${command.id} (${command.name}) has blank summary", command.summary.isBlank())
            assertFalse("Command ${command.id} (${command.name}) has blank description", command.description.isBlank())
            assertFalse("Command ${command.id} (${command.name}) has blank source citation", command.source.isBlank())
        }
    }

    @Test
    fun `glossary terms are non-blank and have no duplicate terms`() {
        val terms = AllGlossary.terms.map { it.term }
        assertEquals("Duplicate glossary term found", terms.size, terms.toSet().size)
        for (entry in AllGlossary.terms) {
            assertFalse(entry.term.isBlank())
            assertFalse(entry.definition.isBlank())
        }
    }

    @Test
    fun `expected minimum content volume is present`() {
        val chapterCount = AllParts.parts.sumOf { it.chapters.size }
        assertTrue("Expected at least 30 chapters, found $chapterCount", chapterCount >= 30)
        assertTrue("Expected at least 90 commands, found ${AllCommands.commands.size}", AllCommands.commands.size >= 90)
    }

    @Test
    fun `data structure ids are unique and every structure has fields`() {
        val ids = AllDataStructures.structures.map { it.id }
        assertEquals("Duplicate data structure id found", ids.size, ids.toSet().size)
        for (structure in AllDataStructures.structures) {
            assertFalse("Structure ${structure.id} has blank name", structure.name.isBlank())
            assertFalse("Structure ${structure.id} (${structure.name}) has blank summary", structure.summary.isBlank())
            assertFalse("Structure ${structure.id} (${structure.name}) has blank source citation", structure.source.isBlank())
            assertTrue("Structure ${structure.id} (${structure.name}) has no fields", structure.fields.isNotEmpty())
            for (field in structure.fields) {
                assertFalse("A field in structure ${structure.id} (${structure.name}) has a blank range", field.range.isBlank())
                assertFalse("A field in structure ${structure.id} (${structure.name}) has a blank field name", field.fieldName.isBlank())
                assertFalse("A field in structure ${structure.id} (${structure.name}) has a blank description", field.fieldDescription.isBlank())
            }
        }
    }
}

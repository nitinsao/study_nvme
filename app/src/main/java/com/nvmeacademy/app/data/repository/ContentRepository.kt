package com.nvmeacademy.app.data.repository

import com.nvmeacademy.app.data.content.AllCommands
import com.nvmeacademy.app.data.content.AllDataStructures
import com.nvmeacademy.app.data.content.AllGlossary
import com.nvmeacademy.app.data.content.AllParts
import com.nvmeacademy.app.data.db.AppDatabase
import com.nvmeacademy.app.data.db.entities.ChapterEntity
import com.nvmeacademy.app.data.db.entities.CommandEntity
import com.nvmeacademy.app.data.db.entities.CommandFieldEntity
import com.nvmeacademy.app.data.db.entities.DataStructureEntity
import com.nvmeacademy.app.data.db.entities.DataStructureFieldEntity
import com.nvmeacademy.app.data.db.entities.GlossaryEntity
import com.nvmeacademy.app.data.db.entities.PartEntity
import com.nvmeacademy.app.data.db.entities.SlideEntity
import kotlinx.coroutines.flow.Flow

class ContentRepository(private val db: AppDatabase) {

    suspend fun seedIfEmpty() {
        if (db.partDao().count() > 0) return

        val parts = mutableListOf<PartEntity>()
        val chapters = mutableListOf<ChapterEntity>()
        val slides = mutableListOf<SlideEntity>()

        for (partSeed in AllParts.parts) {
            parts += PartEntity(partSeed.id, partSeed.order, partSeed.title, partSeed.subtitle)
            for (chapterSeed in partSeed.chapters) {
                chapters += ChapterEntity(
                    id = chapterSeed.id,
                    partId = chapterSeed.partId,
                    order = chapterSeed.order,
                    title = chapterSeed.title,
                    shortDescription = chapterSeed.shortDescription,
                    level = chapterSeed.level
                )
                for (slideSeed in chapterSeed.slides) {
                    slides += SlideEntity(
                        chapterId = chapterSeed.id,
                        order = slideSeed.order,
                        title = slideSeed.title,
                        bulletPoints = slideSeed.bullets.joinToString("\n"),
                        detailedNotes = slideSeed.notes,
                        sourceCitation = slideSeed.source
                    )
                }
            }
        }

        val commands = AllCommands.commands.map { c ->
            CommandEntity(
                id = c.id,
                opcode = c.opcode,
                name = c.name,
                commandSet = c.commandSet,
                summary = c.summary,
                description = c.description,
                mandatory = c.mandatory,
                sourceCitation = c.source
            )
        }
        val fields = AllCommands.commands.flatMap { c ->
            c.fields.mapIndexed { index, f ->
                CommandFieldEntity(commandId = c.id, order = index, fieldName = f.name, fieldDescription = f.description)
            }
        }

        val glossary = AllGlossary.terms.map { GlossaryEntity(term = it.term, definition = it.definition) }

        val structures = AllDataStructures.structures.map { s ->
            DataStructureEntity(
                id = s.id,
                order = s.order,
                category = s.category,
                name = s.name,
                summary = s.summary,
                sourceCitation = s.source
            )
        }
        val structureFields = AllDataStructures.structures.flatMap { s ->
            s.fields.mapIndexed { index, f ->
                DataStructureFieldEntity(structureId = s.id, order = index, range = f.range, fieldName = f.fieldName, fieldDescription = f.fieldDescription)
            }
        }

        db.partDao().insertAll(parts)
        db.chapterDao().insertAll(chapters)
        db.slideDao().insertAll(slides)
        db.commandDao().insertAll(commands)
        db.commandFieldDao().insertAll(fields)
        db.dataStructureDao().insertAll(structures)
        db.dataStructureFieldDao().insertAll(structureFields)
        db.glossaryDao().insertAll(glossary)
    }

    fun observeParts(): Flow<List<PartEntity>> = db.partDao().observeAll()
    fun observeChaptersByPart(partId: Int): Flow<List<ChapterEntity>> = db.chapterDao().observeByPart(partId)
    suspend fun getChapter(chapterId: Int): ChapterEntity? = db.chapterDao().getById(chapterId)
    fun observeSlides(chapterId: Int): Flow<List<SlideEntity>> = db.slideDao().observeByChapter(chapterId)

    fun searchCommands(query: String): Flow<List<CommandEntity>> = db.commandDao().search(query)
    fun observeAllCommands(): Flow<List<CommandEntity>> = db.commandDao().observeAll()
    suspend fun getCommand(id: Int): CommandEntity? = db.commandDao().getById(id)
    fun observeCommandFields(commandId: Int): Flow<List<CommandFieldEntity>> = db.commandFieldDao().observeByCommand(commandId)

    fun searchGlossary(query: String): Flow<List<GlossaryEntity>> = db.glossaryDao().search(query)
    fun observeGlossary(): Flow<List<GlossaryEntity>> = db.glossaryDao().observeAll()

    fun observeDataStructures(): Flow<List<DataStructureEntity>> = db.dataStructureDao().observeAll()
    fun searchDataStructures(query: String): Flow<List<DataStructureEntity>> = db.dataStructureDao().search(query)
    suspend fun getDataStructure(id: Int): DataStructureEntity? = db.dataStructureDao().getById(id)
    fun observeDataStructureFields(structureId: Int): Flow<List<DataStructureFieldEntity>> = db.dataStructureFieldDao().observeByStructure(structureId)
}

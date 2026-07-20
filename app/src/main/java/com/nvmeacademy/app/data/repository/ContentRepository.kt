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
import com.nvmeacademy.app.data.progress.ProgressStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** One swipeable card in the Learn deck: a slide plus the chapter/part it belongs to. */
data class DeckCard(
    val slide: SlideEntity,
    val chapter: ChapterEntity,
    val partTitle: String,
    val position: Int,
    val total: Int
)

class ContentRepository(private val db: AppDatabase, private val progressStore: ProgressStore) {

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
                    val diagram = slideSeed.diagram
                    slides += SlideEntity(
                        chapterId = chapterSeed.id,
                        order = slideSeed.order,
                        title = slideSeed.title,
                        bulletPoints = slideSeed.bullets.joinToString("\n"),
                        detailedNotes = slideSeed.notes,
                        sourceCitation = slideSeed.source,
                        diagramCaption = diagram?.caption.orEmpty(),
                        diagramOrientation = diagram?.orientation ?: "H",
                        diagramConnector = diagram?.connector ?: "arrow",
                        diagramSteps = diagram?.steps.orEmpty().joinToString("\n") { "${it.label}::${it.sublabel}::${it.weight}" }
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

    /** The full Learn deck (every slide across every chapter/part) in reading order. */
    fun observeDeck(): Flow<List<DeckCard>> = combine(
        db.chapterDao().observeAllOrderedGlobally(),
        db.slideDao().observeAllOrderedGlobally(),
        db.partDao().observeAll()
    ) { chapters, slides, parts ->
        val chapterById = chapters.associateBy { it.id }
        val partTitleById = parts.associateBy({ it.id }, { it.title })
        slides.mapIndexedNotNull { index, slide ->
            val chapter = chapterById[slide.chapterId] ?: return@mapIndexedNotNull null
            DeckCard(
                slide = slide,
                chapter = chapter,
                partTitle = partTitleById[chapter.partId].orEmpty(),
                position = index + 1,
                total = slides.size
            )
        }
    }

    suspend fun getFirstChapterId(): Int? = db.chapterDao().getFirstChapterId()
    fun observeLastChapterId(): Flow<Int?> = progressStore.lastChapterId
    suspend fun saveLastChapter(chapterId: Int) = progressStore.saveLastChapter(chapterId)
    suspend fun clearProgress() = progressStore.clear()

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

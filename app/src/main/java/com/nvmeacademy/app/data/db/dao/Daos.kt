package com.nvmeacademy.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nvmeacademy.app.data.db.entities.ChapterEntity
import com.nvmeacademy.app.data.db.entities.CommandEntity
import com.nvmeacademy.app.data.db.entities.CommandFieldEntity
import com.nvmeacademy.app.data.db.entities.DataStructureEntity
import com.nvmeacademy.app.data.db.entities.DataStructureFieldEntity
import com.nvmeacademy.app.data.db.entities.GlossaryEntity
import com.nvmeacademy.app.data.db.entities.PartEntity
import com.nvmeacademy.app.data.db.entities.SlideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parts: List<PartEntity>)

    @Query("SELECT * FROM parts ORDER BY `order` ASC")
    fun observeAll(): Flow<List<PartEntity>>

    @Query("SELECT COUNT(*) FROM parts")
    suspend fun count(): Int
}

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters WHERE partId = :partId ORDER BY `order` ASC")
    fun observeByPart(partId: Int): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getById(chapterId: Int): ChapterEntity?

    @Query(
        """
        SELECT chapters.* FROM chapters
        JOIN parts ON chapters.partId = parts.id
        ORDER BY parts.`order` ASC, chapters.`order` ASC
        """
    )
    fun observeAllOrderedGlobally(): Flow<List<ChapterEntity>>

    @Query(
        """
        SELECT chapters.id FROM chapters
        JOIN parts ON chapters.partId = parts.id
        ORDER BY parts.`order` ASC, chapters.`order` ASC
        LIMIT 1
        """
    )
    suspend fun getFirstChapterId(): Int?
}

@Dao
interface SlideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slides: List<SlideEntity>)

    @Query("SELECT * FROM slides WHERE chapterId = :chapterId ORDER BY `order` ASC")
    fun observeByChapter(chapterId: Int): Flow<List<SlideEntity>>

    @Query(
        """
        SELECT slides.* FROM slides
        JOIN chapters ON slides.chapterId = chapters.id
        JOIN parts ON chapters.partId = parts.id
        ORDER BY parts.`order` ASC, chapters.`order` ASC, slides.`order` ASC
        """
    )
    fun observeAllOrderedGlobally(): Flow<List<SlideEntity>>
}

@Dao
interface CommandDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(commands: List<CommandEntity>)

    @Query("SELECT COUNT(*) FROM commands")
    suspend fun count(): Int

    @Query("SELECT * FROM commands ORDER BY commandSet ASC, opcode ASC")
    fun observeAll(): Flow<List<CommandEntity>>

    @Query(
        """
        SELECT * FROM commands
        WHERE name LIKE '%' || :query || '%'
           OR opcode LIKE '%' || :query || '%'
           OR summary LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN name LIKE :query || '%' THEN 0 ELSE 1 END,
            commandSet ASC, opcode ASC
        """
    )
    fun search(query: String): Flow<List<CommandEntity>>

    @Query("SELECT * FROM commands WHERE id = :id")
    suspend fun getById(id: Int): CommandEntity?
}

@Dao
interface CommandFieldDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fields: List<CommandFieldEntity>)

    @Query("SELECT * FROM command_fields WHERE commandId = :commandId ORDER BY `order` ASC")
    fun observeByCommand(commandId: Int): Flow<List<CommandFieldEntity>>
}

@Dao
interface DataStructureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(structures: List<DataStructureEntity>)

    @Query("SELECT * FROM data_structures ORDER BY `order` ASC")
    fun observeAll(): Flow<List<DataStructureEntity>>

    @Query(
        """
        SELECT * FROM data_structures
        WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%'
        ORDER BY `order` ASC
        """
    )
    fun search(query: String): Flow<List<DataStructureEntity>>

    @Query("SELECT * FROM data_structures WHERE id = :id")
    suspend fun getById(id: Int): DataStructureEntity?
}

@Dao
interface DataStructureFieldDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fields: List<DataStructureFieldEntity>)

    @Query("SELECT * FROM data_structure_fields WHERE structureId = :structureId ORDER BY `order` ASC")
    fun observeByStructure(structureId: Int): Flow<List<DataStructureFieldEntity>>
}

@Dao
interface GlossaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(terms: List<GlossaryEntity>)

    @Query("SELECT * FROM glossary ORDER BY term ASC")
    fun observeAll(): Flow<List<GlossaryEntity>>

    @Query("SELECT * FROM glossary WHERE term LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%' ORDER BY term ASC")
    fun search(query: String): Flow<List<GlossaryEntity>>
}

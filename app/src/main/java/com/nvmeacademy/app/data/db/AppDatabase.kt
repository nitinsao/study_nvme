package com.nvmeacademy.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nvmeacademy.app.data.db.dao.ChapterDao
import com.nvmeacademy.app.data.db.dao.CommandDao
import com.nvmeacademy.app.data.db.dao.CommandFieldDao
import com.nvmeacademy.app.data.db.dao.GlossaryDao
import com.nvmeacademy.app.data.db.dao.PartDao
import com.nvmeacademy.app.data.db.dao.SlideDao
import com.nvmeacademy.app.data.db.entities.ChapterEntity
import com.nvmeacademy.app.data.db.entities.CommandEntity
import com.nvmeacademy.app.data.db.entities.CommandFieldEntity
import com.nvmeacademy.app.data.db.entities.GlossaryEntity
import com.nvmeacademy.app.data.db.entities.PartEntity
import com.nvmeacademy.app.data.db.entities.SlideEntity

@Database(
    entities = [
        PartEntity::class,
        ChapterEntity::class,
        SlideEntity::class,
        CommandEntity::class,
        CommandFieldEntity::class,
        GlossaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partDao(): PartDao
    abstract fun chapterDao(): ChapterDao
    abstract fun slideDao(): SlideDao
    abstract fun commandDao(): CommandDao
    abstract fun commandFieldDao(): CommandFieldDao
    abstract fun glossaryDao(): GlossaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nvme_academy.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

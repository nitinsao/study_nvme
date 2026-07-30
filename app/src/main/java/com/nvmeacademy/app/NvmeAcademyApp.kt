package com.nvmeacademy.app

import android.app.Application
import com.nvmeacademy.app.data.db.AppDatabase
import com.nvmeacademy.app.data.progress.ProgressStore
import com.nvmeacademy.app.data.repository.ContentRepository

class NvmeAcademyApp : Application() {
    lateinit var repository: ContentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = ContentRepository(db, ProgressStore(this))
    }
}

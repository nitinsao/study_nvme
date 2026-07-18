package com.nvmeacademy.app.data

import androidx.compose.runtime.staticCompositionLocalOf
import com.nvmeacademy.app.data.repository.ContentRepository

val LocalContentRepository = staticCompositionLocalOf<ContentRepository> {
    error("ContentRepository not provided")
}

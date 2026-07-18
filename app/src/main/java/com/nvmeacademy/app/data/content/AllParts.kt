package com.nvmeacademy.app.data.content

/** Aggregates every Part (and its nested Chapters/Slides) that ships with the app. */
object AllParts {
    val parts: List<PartSeed> = listOf(
        Part1Foundations.part,
        Part2Architecture.part,
        Part3AdminCommands.part,
        Part4NvmIoCommands.part,
        Part5AdvancedTopics.part,
        Part6NvmeMi.part
    )
}

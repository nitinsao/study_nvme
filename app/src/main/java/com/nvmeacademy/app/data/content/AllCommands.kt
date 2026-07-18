package com.nvmeacademy.app.data.content

import com.nvmeacademy.app.data.db.entities.CommandSet

/** Aggregates every command reference entry. Populated from the real NVMe specs. */
object AllCommands {
    val commands: List<CommandSeed> = AdminCommands.list + NvmIoCommands.list + FabricsCommands.list +
        AdminExtCommands.list + MiCommands.list
}

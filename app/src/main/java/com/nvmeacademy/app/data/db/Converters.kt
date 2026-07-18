package com.nvmeacademy.app.data.db

import androidx.room.TypeConverter
import com.nvmeacademy.app.data.db.entities.CommandSet

class Converters {
    @TypeConverter
    fun fromCommandSet(value: CommandSet): String = value.name

    @TypeConverter
    fun toCommandSet(value: String): CommandSet = CommandSet.valueOf(value)
}

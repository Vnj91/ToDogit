package com.example.todo91.data

import androidx.room.TypeConverter
import java.util.UUID

class Converters {

    @TypeConverter
    fun fromUuid(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUuid(uuidString: String?): UUID? {
        return uuidString?.let { UUID.fromString(it) }
    }
}

package com.example.todo91.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = false)
    val id: UUID,

    val task: String,
    var isCompleted: Boolean,
    val colorHex: String
) {
    constructor(task: String, isCompleted: Boolean = false, colorHex: String) :
            this(UUID.randomUUID(), task, isCompleted, colorHex)
}

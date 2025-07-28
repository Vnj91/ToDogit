package com.example.todo91.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Todo(
    @DocumentId
    val id: String? = null,
    val title: String = "",
    val task: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isCompleted: Boolean = false,
    val colorIndex: Int = 0,

    @ServerTimestamp
    val timestamp: Timestamp? = null,

    @ServerTimestamp
    val lastEdited: Timestamp? = null,

    val reminderTime: Timestamp? = null
)
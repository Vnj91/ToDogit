package com.example.todo91.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

// Data class for a single item in a checklist
data class ChecklistItem(
    val text: String = "",
    // Explicitly map the 'isChecked' property to the 'checked' field in Firestore.
    @get:PropertyName("isChecked") @set:PropertyName("isChecked")
    var isChecked: Boolean = false
)

data class Todo(
    @DocumentId
    val id: String? = null,
    val title: String = "",
    val task: String = "", // Used for note content if not a checklist

    // Add @PropertyName to all boolean fields starting with "is"
    @get:PropertyName("isPinned") @set:PropertyName("isPinned")
    var isPinned: Boolean = false,

    @get:PropertyName("isArchived") @set:PropertyName("isArchived")
    var isArchived: Boolean = false,

    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,

    val colorIndex: Int = 0,

    @ServerTimestamp
    val timestamp: Timestamp? = null,

    @ServerTimestamp
    val lastEdited: Timestamp? = null,

    val reminderTime: Timestamp? = null,

    // A list to hold checklist items. If null, it's a regular note.
    val checklistItems: List<ChecklistItem>? = null
)

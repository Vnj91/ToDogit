package com.example.todo91.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo91.model.SortOrder
import com.example.todo91.model.Todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _currentSortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val currentSortOrder: StateFlow<SortOrder> = _currentSortOrder.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorLoadingTasks = MutableStateFlow<String?>(null)
    val errorLoadingTasks: StateFlow<String?> = _errorLoadingTasks.asStateFlow()

    private val userIdFlow = MutableStateFlow(auth.currentUser?.uid)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            userIdFlow.value = firebaseAuth.currentUser?.uid
        }
    }

    private val allNotesFlow: StateFlow<List<Todo>> = userIdFlow.flatMapLatest { userId ->
        _isLoading.value = true
        if (userId == null) {
            _isLoading.value = false
            flowOf(emptyList())
        } else {
            firestore.collection("users").document(userId).collection("tasks")
                .snapshots()
                .map { snapshot ->
                    _isLoading.value = false
                    snapshot.toObjects<Todo>()
                }
                .catch {
                    _errorLoadingTasks.value = "Failed to load notes: ${it.message}"
                    _isLoading.value = false
                    emit(emptyList())
                }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val todos: Flow<List<Todo>> = combine(allNotesFlow, _currentSortOrder) { allNotes, sortOrder ->
        val unarchived = allNotes.filter { !it.isArchived }
        sort(unarchived, sortOrder)
    }

    val archivedTodos: Flow<List<Todo>> = combine(allNotesFlow, _currentSortOrder) { allNotes, sortOrder ->
        val archived = allNotes.filter { it.isArchived }
        sort(archived, sortOrder, isArchiveScreen = true)
    }

    private fun sort(notes: List<Todo>, sortOrder: SortOrder, isArchiveScreen: Boolean = false): List<Todo> {
        val pinned = if (isArchiveScreen) emptyList() else notes.filter { it.isPinned }
        val unpinned = if (isArchiveScreen) notes else notes.filter { !it.isPinned }

        val sortedUnpinned = when(sortOrder) {
            SortOrder.NEWEST_FIRST -> unpinned.sortedByDescending { it.lastEdited }
            SortOrder.OLDEST_FIRST -> unpinned.sortedBy { it.lastEdited }
            SortOrder.ALPHABETICAL_ASC -> unpinned.sortedBy { it.title.lowercase() }
            SortOrder.ALPHABETICAL_DESC -> unpinned.sortedByDescending { it.title.lowercase() }
            SortOrder.COMPLETED_FIRST -> unpinned.sortedByDescending { it.isCompleted }
            SortOrder.INCOMPLETE_FIRST -> unpinned.sortedBy { it.isCompleted }
        }
        return pinned + sortedUnpinned
    }

    fun getTodoById(todoId: String): Flow<Todo?> {
        return allNotesFlow.map { notes ->
            notes.find { it.id == todoId }
        }
    }

    fun setSortOrder(order: SortOrder) { _currentSortOrder.value = order }

    fun addTodo(title: String, task: String, colorIndex: Int) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val newTodo = Todo(
                    title = title,
                    task = task,
                    colorIndex = colorIndex,
                    isArchived = false,
                    isPinned = false
                )
                firestore.collection("users").document(userId).collection("tasks").add(newTodo).await()
            } catch (e: Exception) {
                _errorLoadingTasks.value = "Failed to add note: ${e.message}"
            }
        }
    }

    fun updateTodo(todo: Todo) {
        val userId = auth.currentUser?.uid
        if (userId != null && todo.id != null) {
            viewModelScope.launch {
                try {
                    val todoRef = firestore.collection("users").document(userId).collection("tasks").document(todo.id)
                    val updates = mapOf(
                        "title" to todo.title,
                        "task" to todo.task,
                        "isCompleted" to todo.isCompleted,
                        "isPinned" to todo.isPinned,
                        "isArchived" to todo.isArchived,
                        "colorIndex" to todo.colorIndex,
                        "reminderTime" to todo.reminderTime,
                        "lastEdited" to FieldValue.serverTimestamp()
                    )
                    todoRef.update(updates).await()
                } catch (e: Exception) { _errorLoadingTasks.value = "Failed to update note: ${e.message}" }
            }
        }
    }

    fun toggleTodoCompletion(todo: Todo) { if (todo.id != null) updateTodo(todo.copy(isCompleted = !todo.isCompleted)) }
    fun togglePinStatus(todo: Todo) { if (todo.id != null) updateTodo(todo.copy(isPinned = !todo.isPinned)) }
    fun toggleArchiveStatus(todo: Todo) { if (todo.id != null) updateTodo(todo.copy(isArchived = !todo.isArchived, isPinned = false)) }

    fun pinTodos(ids: Set<String>, pinStatus: Boolean) {
        runBatchUpdate(ids) { batch, docRef ->
            batch.update(docRef, mapOf(
                "isPinned" to pinStatus,
                "lastEdited" to FieldValue.serverTimestamp()
            ))
        }
    }
    fun archiveTodos(ids: Set<String>, archiveStatus: Boolean) {
        runBatchUpdate(ids) { batch, docRef ->
            batch.update(docRef, mapOf(
                "isArchived" to archiveStatus,
                "isPinned" to false,
                "lastEdited" to FieldValue.serverTimestamp()
            ))
        }
    }
    fun deleteTodos(ids: Set<String>) { runBatchUpdate(ids) { batch, docRef -> batch.delete(docRef) } }

    private fun runBatchUpdate(ids: Set<String>, operation: (WriteBatch, com.google.firebase.firestore.DocumentReference) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                val collection = firestore.collection("users").document(userId).collection("tasks")
                ids.forEach { id -> operation(batch, collection.document(id)) }
                batch.commit().await()
            } catch (e: Exception) { _errorLoadingTasks.value = "Batch operation failed: ${e.message}" }
        }
    }
}
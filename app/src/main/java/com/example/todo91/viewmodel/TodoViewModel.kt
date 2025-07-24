package com.example.todo91.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo91.model.SortOrder
import com.example.todo91.model.Todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _currentUserId = MutableStateFlow<String?>(null)
    private val _currentSortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val currentSortOrder: StateFlow<SortOrder> = _currentSortOrder.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorLoadingTasks = MutableStateFlow<String?>(null)
    val errorLoadingTasks: StateFlow<String?> = _errorLoadingTasks.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserId.value = firebaseAuth.currentUser?.uid
        }
    }

    val todos: Flow<List<Todo>> = createTodoFlow(isArchived = false)
    val archivedTodos: Flow<List<Todo>> = createTodoFlow(isArchived = true)

    private fun createTodoFlow(isArchived: Boolean): Flow<List<Todo>> {
        return combine(_currentUserId, _currentSortOrder) { userId, sortOrder ->
            userId to sortOrder
        }.flatMapLatest { (userId, sortOrder) ->
            if (userId == null) {
                _isLoading.value = false
                return@flatMapLatest flowOf(emptyList())
            }
            val tasksCollection = firestore.collection("users").document(userId).collection("tasks")
            var query: Query = tasksCollection.whereEqualTo("isArchived", isArchived)

            if (!isArchived) {
                query = query.orderBy("isPinned", Query.Direction.DESCENDING)
            }

            query = when (sortOrder) {
                SortOrder.CUSTOM -> query.orderBy("lastEdited", Query.Direction.DESCENDING) // Fallback for now
                SortOrder.NEWEST_FIRST -> query.orderBy("lastEdited", Query.Direction.DESCENDING)
                SortOrder.OLDEST_FIRST -> query.orderBy("lastEdited", Query.Direction.ASCENDING)
                SortOrder.ALPHABETICAL_ASC -> query.orderBy("title", Query.Direction.ASCENDING)
                SortOrder.ALPHABETICAL_DESC -> query.orderBy("title", Query.Direction.DESCENDING)
                SortOrder.COMPLETED_FIRST -> query.orderBy("isCompleted", Query.Direction.DESCENDING)
                SortOrder.INCOMPLETE_FIRST -> query.orderBy("isCompleted", Query.Direction.ASCENDING)
            }

            callbackFlow {
                _isLoading.value = true
                val registration = query.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _errorLoadingTasks.value = "Firestore query failed. Check logs for index link."
                        close(e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        trySend(snapshot.toObjects(Todo::class.java)).isSuccess
                        _errorLoadingTasks.value = null
                    }
                    _isLoading.value = false
                }
                awaitClose { registration.remove() }
            }
        }
    }

    fun getTodoById(todoId: String): Flow<Todo?> {
        val userId = auth.currentUser?.uid
        if (userId == null) return flowOf(null)
        return callbackFlow {
            val docRef = firestore.collection("users").document(userId).collection("tasks").document(todoId)
            val listener = docRef.addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(Todo::class.java)).isSuccess
            }
            awaitClose { listener.remove() }
        }
    }

    fun setSortOrder(order: SortOrder) { _currentSortOrder.value = order }

    fun addTodo(title: String, task: String, colorIndex: Int) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val newTodo = Todo(title = title, task = task, isCompleted = false, colorIndex = colorIndex)
                firestore.collection("users").document(userId).collection("tasks").add(newTodo).await()
            } catch (e: Exception) {
                _errorLoadingTasks.value = "Failed to add task: ${e.message}"
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
                        "title" to todo.title, "task" to todo.task, "isCompleted" to todo.isCompleted,
                        "isPinned" to todo.isPinned, "isArchived" to todo.isArchived, "colorIndex" to todo.colorIndex,
                        "lastEdited" to FieldValue.serverTimestamp()
                    )
                    todoRef.update(updates).await()
                } catch (e: Exception) { _errorLoadingTasks.value = "Failed to update task: ${e.message}" }
            }
        }
    }

    fun toggleTodoCompletion(todo: Todo) { if (todo.id != null) updateTodo(todo.copy(isCompleted = !todo.isCompleted)) }
    fun togglePinStatus(todo: Todo) { if (todo.id != null) updateTodo(todo.copy(isPinned = !todo.isPinned)) }
    fun toggleArchiveStatus(todo: Todo) { if (todo.id != null) updateTodo(todo.copy(isArchived = !todo.isArchived, isPinned = false)) }

    fun pinTodos(ids: Set<String>, pinStatus: Boolean) { runBatchUpdate(ids) { batch, docRef -> batch.update(docRef, "isPinned", pinStatus) } }
    fun archiveTodos(ids: Set<String>, archiveStatus: Boolean) { runBatchUpdate(ids) { batch, docRef -> batch.update(docRef, "isArchived", archiveStatus, "isPinned", false) } }
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
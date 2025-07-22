package com.example.todo91.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo91.data.AppDatabase
import com.example.todo91.data.Todo
import com.example.todo91.data.SortOrder
import com.example.todo91.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val todoDao = AppDatabase.getDatabase(application).todoDao()

    private val _currentSortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val currentSortOrder: StateFlow<SortOrder> = _currentSortOrder.asStateFlow()

    val todos: Flow<List<Todo>> = todoDao.getAllTodos().combine(_currentSortOrder) { todos, sortOrder ->
        when (sortOrder) {
            SortOrder.NEWEST_FIRST -> todos.sortedByDescending { it.id }
            SortOrder.OLDEST_FIRST -> todos.sortedBy { it.id }
            SortOrder.ALPHABETICAL_ASC -> todos.sortedBy { it.task }
            SortOrder.ALPHABETICAL_DESC -> todos.sortedByDescending { it.task }
            SortOrder.COMPLETED_FIRST -> todos.sortedWith(compareByDescending<Todo> { it.isCompleted }.thenByDescending { it.id })
            SortOrder.INCOMPLETE_FIRST -> todos.sortedWith(compareBy<Todo> { it.isCompleted }.thenByDescending { it.id })
        }
    }

    fun setSortOrder(order: SortOrder) {
        _currentSortOrder.value = order
    }

    // Modified addTodo to accept task string, and it handles color internally
    fun addTodo(task: String) {
        if (task.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                val randomColorIndex = AppColors.TaskBackgroundColors.indices.random()
                val newTodo = Todo(id = UUID.randomUUID(), task = task, isCompleted = false, colorIndex = randomColorIndex)
                todoDao.insertTodo(newTodo)
            }
        }
    }

    // NEW: Generic update function that takes a full Todo object
    fun updateTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.updateTodo(todo)
        }
    }

    // Original toggleTodoCompletion can now call the generic updateTodo
    fun toggleTodoCompletion(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
            todoDao.updateTodo(updatedTodo) // Use the new generic updateTodo
        }
    }

    // Original updateTodoTask can now call the generic updateTodo
    fun updateTodoTask(todo: Todo, newTaskText: String) {
        if (newTaskText.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                val updatedTodo = todo.copy(task = newTaskText)
                todoDao.updateTodo(updatedTodo) // Use the new generic updateTodo
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.deleteTodo(todo)
        }
    }

    fun deleteAllTodos() {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.deleteAllTodos()
        }
    }
}
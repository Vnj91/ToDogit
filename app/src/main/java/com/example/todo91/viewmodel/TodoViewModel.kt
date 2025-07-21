package com.example.todo91.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo91.data.AppDatabase
import com.example.todo91.data.Todo
import com.example.todo91.ui.theme.AppColors
import com.example.todo91.ui.theme.toArgb
import com.example.todo91.ui.theme.toHexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val todoDao = AppDatabase.getDatabase(application).todoDao()

    val todos: Flow<List<Todo>> = todoDao.getAllTodos()

    fun addTodo(task: String) {
        if (task.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                val randomColorHex = AppColors.TaskBackgroundColors.random().toArgb().toHexString()
                val newTodo = Todo(id = UUID.randomUUID(), task = task, isCompleted = false, colorHex = randomColorHex)
                todoDao.insertTodo(newTodo)
            }
        }
    }

    fun toggleTodoCompletion(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
            todoDao.updateTodo(updatedTodo)
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

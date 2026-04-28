package com.example.fish_tasks_android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fish_tasks_android.data.*
import com.example.fish_tasks_android.model.Task
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class FishViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = FishDatabase.getDatabase(application).taskDao()
    private val settingsRepository = SettingsRepository(application.dataStore)
    private val repository = FishRepository(taskDao, settingsRepository)

    val allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime = _currentTime.asStateFlow()

    private val _expiredTask = MutableStateFlow<Task?>(null)
    val expiredTask = _expiredTask.asStateFlow()

    private val scoreAdd = mapOf("low" to 10, "medium" to 25, "high" to 50)
    private val scoreLoss = mapOf("low" to 5, "medium" to 15, "high" to 30)

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _currentTime.value = System.currentTimeMillis()
                if (_expiredTask.value == null) {
                    checkExpiries()
                }
            }
        }
    }

    private suspend fun checkExpiries() {
        val now = System.currentTimeMillis()
        val activeTasks = allTasks.value.filter { it.status == "active" }
        for (task in activeTasks) {
            val progress = (now - task.createdAt).toFloat() / (task.deadline - task.createdAt)
            if (progress >= 1f && !task.expiredTriggered) {
                expireTask(task)
                break // Only show one at a time
            }
        }
    }

    private suspend fun expireTask(task: Task) {
        val updatedTask = task.copy(
            expiredTriggered = true,
            expiredAt = System.currentTimeMillis()
        )
        repository.updateTask(updatedTask)

        val currentSettings = settings.value
        val penalty = scoreLoss[task.priority] ?: 0
        repository.updateStats(
            score = (currentSettings.score - penalty).coerceAtLeast(0),
            streak = 0,
            completed = currentSettings.completed,
            expired = currentSettings.expired + 1,
            deleted = currentSettings.deleted
        )
        _expiredTask.value = updatedTask
    }

    fun dismissExpiration(complete: Boolean, task: Task) {
        viewModelScope.launch {
            if (complete) {
                completeTask(task, late = true)
            } else {
                deleteTask(task)
            }
            _expiredTask.value = null
        }
    }

    fun addTask(title: String, category: String, priority: String, deadline: Long, description: String) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                category = category,
                priority = priority,
                deadline = deadline,
                description = description
            )
            repository.insertTask(task)
        }
    }

    fun completeTask(task: Task, late: Boolean = false) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val points = (scoreAdd[task.priority] ?: 0).let { if (late) it / 2 else it }
            
            val updatedTask = task.copy(
                status = "completed",
                completedAt = now,
                completedLate = late,
                scoreAwarded = points
            )
            repository.updateTask(updatedTask)

            val currentSettings = settings.value
            repository.updateStats(
                score = currentSettings.score + points,
                streak = currentSettings.streak + 1,
                completed = currentSettings.completed + 1,
                expired = currentSettings.expired,
                deleted = currentSettings.deleted
            )
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            if (task.status == "active") {
                val updatedTask = task.copy(
                    status = "deleted",
                    deletedAt = System.currentTimeMillis()
                )
                repository.updateTask(updatedTask)
                
                val currentSettings = settings.value
                repository.updateStats(
                    score = currentSettings.score,
                    streak = currentSettings.streak,
                    completed = currentSettings.completed,
                    expired = currentSettings.expired,
                    deleted = currentSettings.deleted + 1
                )
            } else {
                repository.deleteTask(task)
            }
        }
    }

    fun restoreTask(task: Task, newDeadline: Long) {
        viewModelScope.launch {
            val wasCompleted = task.status == "completed"
            val awarded = task.scoreAwarded ?: 0
            
            val updatedTask = task.copy(
                status = "active",
                workStatus = "pending",
                deadline = newDeadline,
                expiredTriggered = false,
                completedAt = null,
                deletedAt = null,
                scoreAwarded = null
            )
            repository.updateTask(updatedTask)

            val currentSettings = settings.value
            if (wasCompleted) {
                repository.updateStats(
                    score = (currentSettings.score - awarded).coerceAtLeast(0),
                    streak = currentSettings.streak, // Streak doesn't decrement in web app
                    completed = (currentSettings.completed - 1).coerceAtLeast(0),
                    expired = currentSettings.expired,
                    deleted = currentSettings.deleted
                )
            } else {
                repository.updateStats(
                    score = currentSettings.score,
                    streak = currentSettings.streak,
                    completed = currentSettings.completed,
                    expired = currentSettings.expired,
                    deleted = (currentSettings.deleted - 1).coerceAtLeast(0)
                )
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun saveSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun toggleWorkStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.workStatus == "in_progress") "pending" else "in_progress"
            repository.updateTask(task.copy(workStatus = newStatus))
        }
    }
}

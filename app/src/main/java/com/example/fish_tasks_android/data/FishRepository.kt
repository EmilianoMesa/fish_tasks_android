package com.example.fish_tasks_android.data

import com.example.fish_tasks_android.model.Task
import kotlinx.coroutines.flow.Flow

class FishRepository(
    private val taskDao: TaskDao,
    private val settingsRepository: SettingsRepository
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val settings: Flow<AppSettings> = settingsRepository.settingsFlow

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    suspend fun clearHistory() = taskDao.clearHistory()

    suspend fun saveSettings(settings: AppSettings) = settingsRepository.saveSettings(settings)
    suspend fun updateStats(score: Int, streak: Int, completed: Int, expired: Int, deleted: Int) =
        settingsRepository.updateStats(score, streak, completed, expired, deleted)
}

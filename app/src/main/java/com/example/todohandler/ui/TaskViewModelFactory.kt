package com.example.todohandler.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todohandler.data.local.SettingsManager
import com.example.todohandler.data.repository.TaskRepository
import com.example.todohandler.notification.TaskAlarmScheduler

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val settingsManager: SettingsManager,
    private val alarmScheduler: TaskAlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, settingsManager, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

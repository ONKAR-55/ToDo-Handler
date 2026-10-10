package com.example.todohandler.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todohandler.data.local.SettingsManager
import com.example.todohandler.data.model.TaskEntity
import com.example.todohandler.data.model.TaskStatus
import com.example.todohandler.data.repository.TaskRepository
import com.example.todohandler.domain.TaskLogic
import com.example.todohandler.notification.TaskAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class TaskViewModel(
    private val repository: TaskRepository,
    private val settingsManager: SettingsManager,
    private val alarmScheduler: TaskAlarmScheduler
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                val now = System.currentTimeMillis()

                tasks.forEach { task ->
                    val endMillis = task.startTimeMillis + (task.durationMinutes * 60 * 1000L)

                    if (task.status == TaskStatus.PENDING) {
                        if (now in task.startTimeMillis..endMillis) {
                            repository.updateTask(task.copy(status = TaskStatus.IN_PROGRESS))
                        } else if (now > endMillis) {
                            repository.updateTask(task.copy(status = TaskStatus.MISSED))
                        }
                    } else if (task.status == TaskStatus.IN_PROGRESS) {
                        if (now > endMillis) {
                            repository.updateTask(task.copy(status = TaskStatus.MISSED))
                        }
                    }
                }

                // If the selected task is updated elsewhere (e.g. status change), reflect it in selectedTask
                _selectedTask.value?.let { currentSelected ->
                    val updatedVersion = tasks.find { it.id == currentSelected.id }
                    if (updatedVersion != null && updatedVersion != currentSelected) {
                        _selectedTask.value = updatedVersion
                    }
                }
            }
        }
    }

    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val theme: StateFlow<String> = settingsManager.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Dark")
    
    fun setTheme(theme: String) {
        viewModelScope.launch { settingsManager.setTheme(theme) }
    }

    val windowStartNotification: StateFlow<Boolean> = settingsManager.windowStartNotificationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
        
    fun setWindowStartNotification(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setWindowStartNotification(enabled) }
    }

    val preEndWrapWarning: StateFlow<Boolean> = settingsManager.preEndWrapWarningFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
        
    fun setPreEndWrapWarning(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setPreEndWrapWarning(enabled) }
    }

    private val _selectedDateMillis = MutableStateFlow(TaskLogic.getStartOfDayMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis

    val tasksForSelectedDate: StateFlow<List<TaskEntity>> = combine(allTasks, selectedDateMillis) { tasks, dateMillis ->
        val endOfDayMillis = TaskLogic.getEndOfDayMillis(dateMillis)
        tasks.filter { it.dateMillis in dateMillis..endOfDayMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedTask = MutableStateFlow<TaskEntity?>(null)
    val selectedTask: StateFlow<TaskEntity?> = _selectedTask

    fun selectTask(task: TaskEntity?) {
        _selectedTask.value = task
    }

    val completedTasksForSelectedDate: StateFlow<List<TaskEntity>> = tasksForSelectedDate.map { tasks ->
        tasks.filter { it.status == TaskStatus.COMPLETED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory
    fun setCategory(category: String) { _selectedCategory.value = category }

    val streakDays: StateFlow<Int> = allTasks.map { tasks ->
        TaskLogic.calculateStreak(tasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addTask(task: TaskEntity) {
        viewModelScope.launch { 
            val id = repository.insertTask(task)
            alarmScheduler.scheduleAlarmsForTask(task.copy(id = id))
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch { 
            repository.updateTask(task)
            if (task.status == TaskStatus.DELETED || task.status == TaskStatus.COMPLETED || task.status == TaskStatus.MISSED) {
                alarmScheduler.cancelAlarmsForTask(task)
            } else {
                alarmScheduler.scheduleAlarmsForTask(task)
            }
        }
    }

    fun selectDate(millis: Long) {
        _selectedDateMillis.value = millis
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: TaskStatus) {
        updateTask(task.copy(status = newStatus))
    }

    fun refreshData() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(900.milliseconds) // Simulate refresh or perform any manual sync/check if needed
        }
    }
}

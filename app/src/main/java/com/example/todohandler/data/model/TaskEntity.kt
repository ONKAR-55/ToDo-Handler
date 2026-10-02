package com.example.todohandler.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED, OVERDUE, MISSED
}

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val dateMillis: Long,
    val startTimeMillis: Long,
    val durationMinutes: Int,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: String = "Personal",
    val reminderEnabled: Boolean = true,
    val reminderRingtoneUri: String? = null
)

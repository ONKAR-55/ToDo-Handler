package com.example.todohandler.domain

import com.example.todohandler.data.model.TaskEntity
import com.example.todohandler.data.model.TaskStatus
import java.util.Calendar

object TaskLogic {

    fun calculateStreak(tasks: List<TaskEntity>): Int {
        if (tasks.isEmpty()) return 0
        val groupedByDay = tasks.groupBy { it.dateMillis / 86400000L }.toSortedMap(reverseOrder())
        var streak = 0
        var missedDays = 0
        for ((_, dayTasks) in groupedByDay) {
            val completed = dayTasks.count { it.status == TaskStatus.COMPLETED }
            val ratio = if (dayTasks.isNotEmpty()) completed.toFloat() / dayTasks.size else 0f
            if (ratio >= 0.7f) {
                streak++
                missedDays = 0
            } else {
                missedDays++
                if (missedDays > 2) break
            }
        }
        return streak
    }

    fun getStartOfDayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfDayMillis(dateMillis: Long): Long {
        return dateMillis + 24 * 60 * 60 * 1000 - 1
    }

    fun calculateTaskProgress(task: TaskEntity): Float {
        if (task.durationMinutes <= 0) return 1f
        val now = System.currentTimeMillis()
        val elapsedMillis = now - task.startTimeMillis
        return (elapsedMillis.toFloat() / (task.durationMinutes * 60 * 1000L)).coerceIn(0f, 1f)
    }

    fun calculateRemainingMinutes(task: TaskEntity): Int {
        val endTimeMillis = task.startTimeMillis + task.durationMinutes * 60 * 1000L
        val remainingMillis = endTimeMillis - System.currentTimeMillis()
        return (remainingMillis / (1000 * 60)).coerceAtLeast(0L).toInt()
    }
    
    fun calculateRemainingSeconds(task: TaskEntity): Int {
        val endTimeMillis = task.startTimeMillis + task.durationMinutes * 60 * 1000L
        val remainingMillis = endTimeMillis - System.currentTimeMillis()
        return (remainingMillis / 1000L).coerceAtLeast(0L).toInt()
    }
}

package com.example.todohandler.domain

import com.example.todohandler.data.model.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun convertTasksToCsv(tasks: List<TaskEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val sb = StringBuilder()

        // CSV Header
        sb.append("ID,Title,Description,Category,Priority,Status,Date,Start Time,Duration (Minutes),Reminder Enabled\n")

        for (task in tasks) {
            val dateStr = dateFormat.format(Date(task.dateMillis))
            val timeStr = timeFormat.format(Date(task.startTimeMillis))

            val row = listOf(
                task.id.toString(),
                escapeCsv(task.title),
                escapeCsv(task.description),
                escapeCsv(task.category),
                task.priority.name,
                task.status.name,
                dateStr,
                timeStr,
                task.durationMinutes.toString(),
                task.reminderEnabled.toString()
            )
            sb.append(row.joinToString(",")).append("\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }
}

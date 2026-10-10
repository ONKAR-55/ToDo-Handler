package com.example.todohandler.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.todohandler.MainActivity
import com.example.todohandler.R
import com.example.todohandler.data.local.AppDatabase
import com.example.todohandler.data.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val taskId = intent.getLongExtra(TaskAlarmScheduler.EXTRA_TASK_ID, -1)
        if (taskId == -1L) return

        if (action == "com.example.todohandler.ACTION_MARK_COMPLETED" ||
            action == "com.example.todohandler.ACTION_MARK_MISSED" ||
            action == "com.example.todohandler.ACTION_MARK_IN_PROGRESS") {
            
            handleAction(context, action, taskId)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.toInt())
            return
        }

        val type = intent.getIntExtra(TaskAlarmScheduler.EXTRA_ALARM_TYPE, -1)
        val title = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE) ?: "Task Reminder"

        showNotification(context, taskId, type, title)
    }

    private fun handleAction(context: Context, action: String, taskId: Long) {
        val dao = AppDatabase.getDatabase(context).taskDao()
        CoroutineScope(Dispatchers.IO).launch {
            val task = dao.getTaskById(taskId)
            if (task != null) {
                when (action) {
                    "com.example.todohandler.ACTION_MARK_COMPLETED" -> {
                        dao.updateTask(task.copy(status = TaskStatus.COMPLETED))
                    }
                    "com.example.todohandler.ACTION_MARK_MISSED" -> {
                        dao.updateTask(task.copy(status = TaskStatus.MISSED))
                    }
                    "com.example.todohandler.ACTION_MARK_IN_PROGRESS" -> {
                        val updatedTask = task.copy(
                            status = TaskStatus.IN_PROGRESS,
                            durationMinutes = task.durationMinutes + 10
                        )
                        dao.updateTask(updatedTask)
                        val scheduler = TaskAlarmScheduler(context)
                        scheduler.scheduleAlarmsForTask(updatedTask)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, taskId: Long, type: Int, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for task reminders"
        }
        notificationManager.createNotificationChannel(channel)

        val message = when (type) {
            TaskAlarmScheduler.TYPE_10_MIN_BEFORE_START -> "Starts in 10 minutes"
            TaskAlarmScheduler.TYPE_ON_START -> "Task is starting now"
            TaskAlarmScheduler.TYPE_10_MIN_BEFORE_END -> "Ends in 10 minutes"
            TaskAlarmScheduler.TYPE_ON_END -> "Time window has ended"
            else -> "Reminder"
        }

        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingContentIntent)
            .setAutoCancel(true)

        if (type == TaskAlarmScheduler.TYPE_ON_END) {
            val completedIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
                action = "com.example.todohandler.ACTION_MARK_COMPLETED"
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            }
            val pendingCompleted = PendingIntent.getBroadcast(
                context, taskId.toInt() * 10 + 1, completedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val missedIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
                action = "com.example.todohandler.ACTION_MARK_MISSED"
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            }
            val pendingMissed = PendingIntent.getBroadcast(
                context, taskId.toInt() * 10 + 2, missedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val inProgressIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
                action = "com.example.todohandler.ACTION_MARK_IN_PROGRESS"
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            }
            val pendingInProgress = PendingIntent.getBroadcast(
                context, taskId.toInt() * 10 + 3, inProgressIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(0, "Completed", pendingCompleted)
            builder.addAction(0, "Missed", pendingMissed)
            builder.addAction(0, "+10 Mins", pendingInProgress)
        }

        notificationManager.notify(taskId.toInt(), builder.build())
    }

    companion object {
        const val CHANNEL_ID = "task_reminders_channel"
    }
}

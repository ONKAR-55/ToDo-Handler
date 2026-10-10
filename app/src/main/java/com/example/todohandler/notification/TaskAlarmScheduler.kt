package com.example.todohandler.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.todohandler.data.model.TaskEntity

class TaskAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarmsForTask(task: TaskEntity) {
        cancelAlarmsForTask(task)
        if (!task.reminderEnabled) return
        
        val now = System.currentTimeMillis()
        val startMillis = task.startTimeMillis
        val endMillis = startMillis + (task.durationMinutes * 60 * 1000L)
        
        // 1. 10 mins before start
        val tenMinBeforeStart = startMillis - 10 * 60 * 1000L
        if (tenMinBeforeStart > now) {
            scheduleAlarm(task, tenMinBeforeStart, TYPE_10_MIN_BEFORE_START)
        }
        
        // 3. On task start
        if (startMillis > now) {
            scheduleAlarm(task, startMillis, TYPE_ON_START)
        }
        
        // 4. 10 mins before end
        val tenMinBeforeEnd = endMillis - 10 * 60 * 1000L
        if (tenMinBeforeEnd > now && task.durationMinutes >= 10) {
            scheduleAlarm(task, tenMinBeforeEnd, TYPE_10_MIN_BEFORE_END)
        }
        
        // 5. On task end
        if (endMillis > now) {
            scheduleAlarm(task, endMillis, TYPE_ON_END)
        }
    }

    private fun scheduleAlarm(task: TaskEntity, timeInMillis: Long, type: Int) {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_ALARM_TYPE, type)
            putExtra(EXTRA_TASK_TITLE, task.title)
        }
        
        val requestCode = (task.id * 100 + type).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("TaskAlarmScheduler", "Exact alarm permission missing", e)
        }
    }

    fun cancelAlarmsForTask(task: TaskEntity) {
        val types = listOf(
            TYPE_10_MIN_BEFORE_START,
            TYPE_ON_START,
            TYPE_10_MIN_BEFORE_END,
            TYPE_ON_END
        )
        
        types.forEach { type ->
            val intent = Intent(context, TaskAlarmReceiver::class.java)
            val requestCode = (task.id * 100 + type).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        
        const val TYPE_10_MIN_BEFORE_START = 1
        const val TYPE_ON_START = 2
        const val TYPE_10_MIN_BEFORE_END = 3
        const val TYPE_ON_END = 4
    }
}

package com.example.todohandler.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todohandler.data.local.AppDatabase
import com.example.todohandler.data.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = TaskAlarmScheduler(context)
            val dao = AppDatabase.getDatabase(context).taskDao()
            
            CoroutineScope(Dispatchers.IO).launch {
                val tasks = dao.getAllTasksSync()
                val now = System.currentTimeMillis()
                
                tasks.forEach { task ->
                    if (task.status == TaskStatus.PENDING || task.status == TaskStatus.IN_PROGRESS) {
                        val endMillis = task.startTimeMillis + (task.durationMinutes * 60 * 1000L)
                        if (endMillis > now) {
                            scheduler.scheduleAlarmsForTask(task)
                        }
                    }
                }
            }
        }
    }
}

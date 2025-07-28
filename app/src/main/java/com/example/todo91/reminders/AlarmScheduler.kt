package com.example.todo91.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.todo91.model.Todo
import java.time.ZoneId

interface AlarmScheduler {
    fun schedule(todo: Todo)
    fun cancel(todo: Todo)
}

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(todo: Todo) {
        // Ensure the ID is not null before proceeding
        if (todo.id == null) return

        val reminderTime = todo.reminderTime?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime() ?: return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", todo.title)
            putExtra("EXTRA_MESSAGE", todo.task)
            putExtra("EXTRA_ID", todo.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todo.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
            pendingIntent
        )
    }

    override fun cancel(todo: Todo) {
        // Ensure the ID is not null before proceeding
        if (todo.id == null) return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todo.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
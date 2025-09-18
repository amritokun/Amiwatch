package com.amiwatch.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.amiwatch.notifications.TaskScheduler.TaskType
import com.amiwatch.notifications.anilist.AnilistNotificationWorker
import com.amiwatch.notifications.comment.CommentNotificationWorker
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName
import com.amiwatch.util.Logger

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = AlarmManagerScheduler(context)
            PrefManager.init(context)
            Logger.init(context)
            Logger.log("Starting Amiwatch Subscription Service on Boot")
            if (PrefManager.getVal(PrefName.UseAlarmManager)) {
                val commentInterval =
                    CommentNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.CommentNotificationInterval)]
                val anilistInterval =
                    AnilistNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.AnilistNotificationInterval)]
                scheduler.scheduleRepeatingTask(
                    TaskType.COMMENT_NOTIFICATION,
                    commentInterval
                )
                scheduler.scheduleRepeatingTask(
                    TaskType.ANILIST_NOTIFICATION,
                    anilistInterval
                )
            }
        }
    }
}

class AlarmPermissionStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            PrefManager.init(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
            if (canScheduleExactAlarms) {
                TaskScheduler.create(context, false).cancelAllTasks()
                TaskScheduler.create(context, true).scheduleAllTasks(context)
            } else {
                TaskScheduler.create(context, true).cancelAllTasks()
                TaskScheduler.create(context, false).scheduleAllTasks(context)
            }
            PrefManager.setVal(PrefName.UseAlarmManager, canScheduleExactAlarms)
        }
    }
}

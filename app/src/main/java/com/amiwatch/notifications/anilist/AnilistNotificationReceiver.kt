package com.amiwatch.notifications.anilist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amiwatch.notifications.AlarmManagerScheduler
import com.amiwatch.notifications.TaskScheduler
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName
import com.amiwatch.util.Logger
import kotlinx.coroutines.runBlocking

class AnilistNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Logger.log("AnilistNotificationReceiver: onReceive")
        runBlocking {
            AnilistNotificationTask().execute(context)
        }
        val anilistInterval =
            AnilistNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.AnilistNotificationInterval)]
        AlarmManagerScheduler(context).scheduleRepeatingTask(
            TaskScheduler.TaskType.ANILIST_NOTIFICATION,
            anilistInterval
        )
    }
}

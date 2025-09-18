package com.amiwatch.notifications.subscription

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amiwatch.notifications.AlarmManagerScheduler
import com.amiwatch.notifications.TaskScheduler
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName
import com.amiwatch.util.Logger
import kotlinx.coroutines.runBlocking

class SubscriptionNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Logger.log("SubscriptionNotificationReceiver: onReceive")
        runBlocking {
            SubscriptionNotificationTask().execute(context)
        }
        val subscriptionInterval =
            SubscriptionNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.SubscriptionNotificationInterval)]
        AlarmManagerScheduler(context).scheduleRepeatingTask(
            TaskScheduler.TaskType.SUBSCRIPTION_NOTIFICATION,
            subscriptionInterval
        )
    }
}

package com.amiwatch.notifications.subscription

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.amiwatch.util.Logger

class SubscriptionNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Logger.log("SubscriptionNotificationWorker: doWork")
        if (System.currentTimeMillis() - lastCheck < 60000) {
            Logger.log("SubscriptionNotificationWorker: doWork skipped")
            return Result.success()
        }
        lastCheck = System.currentTimeMillis()
        return if (SubscriptionNotificationTask().execute(applicationContext)) {
            Result.success()
        } else {
            Logger.log("SubscriptionNotificationWorker: doWork failed")
            Result.retry()
        }
    }

    companion object {
        val checkIntervals = arrayOf(0L, 480, 720, 1440)
        const val WORK_NAME =
            "com.amiwatch.notifications.subscription.SubscriptionNotificationWorker"
        private var lastCheck = 0L
    }
}

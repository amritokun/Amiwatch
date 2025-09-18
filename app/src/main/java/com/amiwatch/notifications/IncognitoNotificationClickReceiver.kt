package com.amiwatch.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amiwatch.INCOGNITO_CHANNEL_ID
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName


class IncognitoNotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        PrefManager.setVal(PrefName.Incognito, false)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(INCOGNITO_CHANNEL_ID)

    }
}

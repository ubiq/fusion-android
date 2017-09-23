package com.ubiqsmart.receiver

import android.content.Context
import android.content.Intent
import com.github.salomonbrys.kodein.android.KodeinBroadcastReceiver
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.services.NotificationLauncher

class BootReceiver : KodeinBroadcastReceiver() {

  private val notificationLauncher: NotificationLauncher by injector.instance()

  override fun onBroadcastReceived(context: Context, intent: Intent) {
    notificationLauncher.start()
  }

}
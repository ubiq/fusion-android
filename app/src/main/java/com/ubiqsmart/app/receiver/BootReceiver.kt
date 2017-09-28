package com.ubiqsmart.app.receiver

import android.content.Context
import android.content.Intent
import com.github.salomonbrys.kodein.android.KodeinBroadcastReceiver
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.app.services.NotificationLauncher

@Deprecated("Replace behavior with android-job library")
class BootReceiver : KodeinBroadcastReceiver() {

  private val notificationLauncher: NotificationLauncher by injector.instance()

  override fun onBroadcastReceived(context: Context, intent: Intent) {
    notificationLauncher.start()
  }

}
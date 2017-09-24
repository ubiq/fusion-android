package com.ubiqsmart.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.ubiqsmart.utils.WalletStorage

import java.util.*

@Deprecated("Replace behavior with android-job library")
class NotificationLauncher private constructor(c: Context, private val walletStorage: WalletStorage) {

  private val context: Context = c.applicationContext
  private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(c)
  private val alarmManager: AlarmManager = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager

  private var pendingIntent: PendingIntent? = null

  fun start() {
    val notificationsNewMessage = preferences.getBoolean("notifications_new_message", true)

    if (notificationsNewMessage && walletStorage.get().size >= 1) {
      val i = Intent(context, NotificationService::class.java)
      pendingIntent = PendingIntent.getService(context, 23, i, 0)

      val syncFrequency = preferences.getString("sync_frequency", "4")
      val syncInt = Integer.parseInt(syncFrequency)

      val now = Date()
      alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.time, AlarmManager.INTERVAL_HOUR * syncInt, pendingIntent)
    }
  }

  fun stop() {
    if (pendingIntent == null) {
      return
    }

    alarmManager.cancel(pendingIntent)
  }

  companion object {

    private var instance: NotificationLauncher? = null

    fun getInstance(c: Context, walletStorage: WalletStorage): NotificationLauncher {
      return instance ?: NotificationLauncher(c, walletStorage).also { instance = it }
    }
  }

}

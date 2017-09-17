package com.ubiqsmart.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.ubiqsmart.services.NotificationLauncher

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationLauncher.getInstance().start(context)
    }

}
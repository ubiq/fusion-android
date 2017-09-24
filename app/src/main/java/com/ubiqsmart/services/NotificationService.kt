package com.ubiqsmart.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import com.github.salomonbrys.kodein.android.KodeinIntentService
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.ubiqsmart.R
import com.ubiqsmart.repository.api.EtherscanAPI
import com.ubiqsmart.ui.main.MainActivity
import com.ubiqsmart.utils.Blockies
import com.ubiqsmart.utils.ExchangeCalculator
import com.ubiqsmart.utils.WalletStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger

class NotificationService : KodeinIntentService(NotificationService::class.java.simpleName) {

  private val notificationManager: NotificationManager by with(applicationContext).instance()
  private val walletStorage: WalletStorage by with(applicationContext).instance()
  private val etherscanApi: EtherscanAPI by instance()
  private val notificationLauncher: NotificationLauncher by with(applicationContext).instance()
  private val preferences: SharedPreferences by withContext(this).instance()

  override fun onHandleIntent(intent: Intent?) {
    val notificationsNewMessage = preferences.getBoolean("notifications_new_message", true)

    if (!notificationsNewMessage || walletStorage.get().size <= 0) {
      notificationLauncher.stop()
      return
    }

    try {
      etherscanApi.getBalances(walletStorage.get(), object : Callback {
        override fun onFailure(call: Call, e: IOException) {}

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
          try {
            val data = JSONObject(response.body()!!.string()).getJSONArray("result")
            var notify = false
            var amount = BigInteger("0")
            var address = ""

            val editor = preferences.edit()

            (0 until data.length()).forEach { i ->
              if (preferences.getString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance")) != data.getJSONObject(i).getString("balance")) {

                if (BigInteger(preferences.getString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance"))).compareTo(BigInteger(data.getJSONObject(i).getString("balance"))) < 1) {
                  notify = true
                  address = data.getJSONObject(i).getString("account")
                  amount = amount.add(BigInteger(data.getJSONObject(i).getString("balance")).subtract(BigInteger(preferences.getString(address, "0"))))
                }

              }

              editor.putString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance"))
            }

            editor.commit()

            if (notify) {
              try {
                val amountS = BigDecimal(amount).divide(ExchangeCalculator.ONE_ETHER, 4, BigDecimal.ROUND_DOWN).toPlainString()
                sendNotification(address, amountS)
              } catch (ignored: Exception) {
              }

            }
          } catch (e: JSONException) {
            e.printStackTrace()
          }
        }
      })
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun sendNotification(address: String, amount: String) {
    val builder = NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_notification)
        .setLargeIcon(Blockies.createIcon(address.toLowerCase()))
        .setColor(0x2d435c)
        .setTicker(getString(R.string.notification_ticker))
        .setLights(Color.CYAN, 3000, 3000)
        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
        .setContentTitle(this.resources.getString(R.string.notification_title))
        .setAutoCancel(true)
        .setContentText(amount + " ETH")
        .setVibrate(longArrayOf(1000, 1000))

    val main = Intent(this, MainActivity::class.java).apply {
      putExtra("STARTAT", 2)
    }

    val contentIntent = PendingIntent.getActivity(this, 0, main, PendingIntent.FLAG_UPDATE_CURRENT)

    builder.setContentIntent(contentIntent)

    val mNotificationId = (Math.random() * 150).toInt()
    notificationManager.notify(mNotificationId, builder.build())
  }

}

package com.ubiqsmart.app.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import com.github.salomonbrys.kodein.android.KodeinIntentService
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.R
import com.ubiqsmart.domain.models.FullWallet
import com.ubiqsmart.app.ui.main.MainActivity
import com.ubiqsmart.app.utils.*
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.CipherException
import org.web3j.crypto.ECKeyPair
import java.io.File
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException

class WalletGenService : KodeinIntentService(WalletGenService::class.java.simpleName) {

  private val notificationManager: NotificationManager by instance()
  private val walletStorage: WalletStorage by instance()
  private val addressNameConverter: AddressNameConverter by instance()

  private lateinit var builder: NotificationCompat.Builder

  private var normalMode = true

  override fun onHandleIntent(intent: Intent?) {
    val password = intent!!.getStringExtra("PASSWORD")
    var privatekey = ""

    if (intent.hasExtra("PRIVATE_KEY")) {
      normalMode = false
      privatekey = intent.getStringExtra("PRIVATE_KEY")
    }

    sendNotification()

    try {
      val walletAddress: String = if (normalMode) { // Create new key
        OwnWalletUtils.generateNewWalletFile(password, File(this.filesDir, ""), true)
      } else { // Privatekey passed
        val keys = ECKeyPair.create(Hex.decode(privatekey))
        OwnWalletUtils.generateWalletFile(password, keys, File(this.filesDir, ""), true)
      }

      walletStorage.add(FullWallet("0x" + walletAddress, walletAddress))
      addressNameConverter.put("0x" + walletAddress, "Wallet " + ("0x" + walletAddress).substring(0, 6))
      Settings.walletBeingGenerated = false

      finished("0x" + walletAddress)
    } catch (e: CipherException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    } catch (e: InvalidAlgorithmParameterException) {
      e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
      e.printStackTrace()
    } catch (e: NoSuchProviderException) {
      e.printStackTrace()
    }
  }

  private fun sendNotification() {
    builder = NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_notification)
        .setColor(0x2d435c)
        .setTicker(if (normalMode) getString(R.string.notification_wallgen_title) else getString(R.string.notification_wallimp_title))
        .setContentTitle(this.resources.getString(if (normalMode) R.string.wallet_gen_service_title else R.string.wallet_gen_service_title_import))
        .setOngoing(true)
        .setProgress(0, 0, true)
        .setContentText(getString(R.string.notification_wallgen_maytake))

    notificationManager.notify(NOTIFICATION_ID, builder.build())
  }

  private fun finished(address: String) {
    builder.setContentTitle(if (normalMode) getString(R.string.notification_wallgen_finished) else getString(R.string.notification_wallimp_finished))
        .setLargeIcon(Blockies.createIcon(address.toLowerCase()))
        .setAutoCancel(true)
        .setLights(Color.CYAN, 3000, 3000)
        .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        .setProgress(100, 100, false)
        .setOngoing(false)
        .setAutoCancel(true)
        .setContentText(getString(R.string.notification_click_to_view))
        .setVibrate(longArrayOf(1000, 1000))

    val main = Intent(this, MainActivity::class.java)
    main.putExtra("STARTAT", 1)

    val contentIntent = PendingIntent.getActivity(this, 0, main, PendingIntent.FLAG_UPDATE_CURRENT)
    builder.setContentIntent(contentIntent)

    notificationManager.notify(NOTIFICATION_ID, builder.build())
  }

  companion object {

    private val NOTIFICATION_ID = 152
  }

}

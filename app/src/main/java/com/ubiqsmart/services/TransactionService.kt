package com.ubiqsmart.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.github.salomonbrys.kodein.android.KodeinIntentService
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.ubiqsmart.R
import com.ubiqsmart.repository.api.EtherscanAPI
import com.ubiqsmart.ui.main.MainActivity
import com.ubiqsmart.utils.ExchangeCalculator
import com.ubiqsmart.utils.WalletStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.methods.request.RawTransaction
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger

class TransactionService : KodeinIntentService(TransactionService::javaClass.name) {

  private lateinit var notificationBuilder: NotificationCompat.Builder

  private val notificationManager: NotificationManager by with(applicationContext).instance()

  private val etherscanApi: EtherscanAPI by instance()
  private val walletStorage: WalletStorage by with(applicationContext).instance()

  override fun onHandleIntent(intent: Intent?) {
    createNotificationBuilder()

    updateNotification()

    try {
      val fromAddress = intent!!.getStringExtra("FROM_ADDRESS")
      val toAddress = intent.getStringExtra("TO_ADDRESS")
      val amount = intent.getStringExtra("AMOUNT")
      val gas_price = intent.getStringExtra("GAS_PRICE")
      val gas_limit = intent.getStringExtra("GAS_LIMIT")
      val data = intent.getStringExtra("DATA")
      val password = intent.getStringExtra("PASSWORD")

      val keys = walletStorage.getFullWallet(password, fromAddress)

      etherscanApi.getNonceForAddress(fromAddress, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          onError(getString(R.string.cant_connect_to_network))
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
          try {
            val rawResponse = response.body()!!.string()
            val o = JSONObject(rawResponse)
            val nonce = BigInteger(o.getString("result").substring(2), 16)

            val tx = RawTransaction.createTransaction(nonce, BigInteger(gas_price), BigInteger(gas_limit), toAddress,
                BigDecimal(amount).multiply(ExchangeCalculator.ONE_ETHER).toBigInteger(), data)

            Log.d("txx", "Nonce: ${tx.nonce}\ngasPrice: ${tx.gasPrice}\ngasLimit: ${tx.gasLimit}\nTo: ${tx.to}\nAmount: ${tx.value}\nData: ${tx.data}")

            val signed = TransactionEncoder.signMessage(tx, 1.toByte(), keys)
            forwardTX(signed)
          } catch (e: Exception) {
            e.printStackTrace()
            onError(getString(R.string.cant_connect_to_network))
          }
        }
      })

    } catch (e: Exception) {
      onError(getString(R.string.invalid_wallet_password))
      e.printStackTrace()
    }
  }

  @Throws(IOException::class)
  private fun forwardTX(signed: ByteArray) {
    etherscanApi.forwardTransaction("0x" + Hex.toHexString(signed), object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        onError(getString(R.string.cant_connect_to_network))
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {
        val received = response.body()!!.string()

        try {
          onSuccess(JSONObject(received).getString("result"))
        } catch (e: Exception) {
          try {
            var errorMessage = JSONObject(received).getJSONObject("onError").getString("message")
            if (errorMessage.indexOf(".") > 0) {
              errorMessage = errorMessage.substring(0, errorMessage.indexOf("."))
            }
            onError(errorMessage) // f.E Insufficient funds
          } catch (e1: JSONException) {
            onError(getString(R.string.uknown_error))
          }
        }
      }
    })
  }

  private fun onSuccess(hash: String) {
    notificationBuilder.setContentTitle(getString(R.string.notification_transfersuc)).setProgress(100, 100, false).setOngoing(false).setAutoCancel(true).setContentText("")

    val main = Intent(this, MainActivity::class.java).apply {
      putExtra("STARTAT", 2)
      putExtra("TXHASH", hash)
    }

    val contentIntent = PendingIntent.getActivity(this, 0, main, PendingIntent.FLAG_UPDATE_CURRENT)
    notificationBuilder.setContentIntent(contentIntent)

    updateNotification()
  }

  private fun onError(err: String) {
    notificationBuilder.setContentTitle(getString(R.string.notification_transferfail))
        .setProgress(100, 100, false)
        .setOngoing(false)
        .setAutoCancel(true)
        .setContentText(err)

    val main = Intent(this, MainActivity::class.java).apply {
      putExtra("STARTAT", 2)
    }

    val contentIntent = PendingIntent.getActivity(this, 0, main, PendingIntent.FLAG_UPDATE_CURRENT)
    notificationBuilder.setContentIntent(contentIntent)

    updateNotification()
  }

  private fun updateNotification() {
    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
  }

  private fun createNotificationBuilder() {
    notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_notification)
        .setColor(0x2d435c)
        .setTicker(getString(R.string.notification_transferingticker))
        .setContentTitle(getString(R.string.notification_transfering_title))
        .setContentText(getString(R.string.notification_might_take_a_minute))
        .setOngoing(true)
        .setProgress(0, 0, true)
  }

  companion object {

    private const val CHANNEL_ID = "general"

    private const val NOTIFICATION_ID = 153
  }

}

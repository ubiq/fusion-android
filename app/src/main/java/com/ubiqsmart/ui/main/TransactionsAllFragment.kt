package com.ubiqsmart.ui.main

import android.view.View
import android.view.View.GONE
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.R
import com.ubiqsmart.interfaces.StorableWallet
import com.ubiqsmart.repository.api.EtherscanAPI
import com.ubiqsmart.repository.data.TransactionDisplay
import com.ubiqsmart.ui.transactions.TransactionsAbstractFragment
import com.ubiqsmart.utils.AddressNameConverter
import com.ubiqsmart.utils.ResponseParser
import com.ubiqsmart.utils.WalletStorage
import kotlinx.android.synthetic.main.fragment_transaction.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.math.BigInteger
import java.util.*

class TransactionsAllFragment : TransactionsAbstractFragment() {

  override val walletStorage: WalletStorage by instance()
  override val addressNameConverter: AddressNameConverter by instance()
  override val etherscanApi: EtherscanAPI by instance()

  private var unconfirmed: TransactionDisplay? = null
  private var unconfirmedAddedTime: Long = 0

  override fun update(force: Boolean) {
    if (!isAdded) {
      return
    }

    wallets.clear()

    swipe_refresh_layout2?.isRefreshing = true

    requestCount = 0

    val storedWallets = ArrayList(walletStorage.get())
    if (storedWallets.size == 0) {
      nothing_found.visibility = View.VISIBLE
      onItemsLoadComplete()
    } else {
      nothing_found.visibility = GONE
      storedWallets.indices.forEach { i ->
        try {
          val currentWallet = storedWallets[i]

          etherscanApi.getNormalTransactions(currentWallet.pubKey, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
              if (isAdded) {
                activity.runOnUiThread {
                  onItemsLoadComplete()
                  (activity as MainActivity).snackError(getString(R.string.no_internet_connection))
                }
              }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
              val restring = response.body()!!.string()

              val w = ArrayList(ResponseParser.parseTransactions(restring, getString(R.string.unnamed_address), currentWallet.pubKey, TransactionDisplay.NORMAL))
              if (isAdded) {
                activity.runOnUiThread { onComplete(w, storedWallets) }
              }
            }
          })

          etherscanApi.getInternalTransactions(currentWallet.pubKey, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
              if (isAdded) {
                activity.runOnUiThread {
                  onItemsLoadComplete()
                  (activity as MainActivity).snackError(getString(R.string.no_internet_connection))
                }
              }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
              val restring = response.body()!!.string()

              val w = ArrayList(ResponseParser.parseTransactions(restring, getString(R.string.unnamed_address), currentWallet.pubKey, TransactionDisplay.CONTRACT))
              if (isAdded) {
                activity.runOnUiThread { onComplete(w, storedWallets) }
              }
            }
          })
        } catch (e: IOException) {
          if (isAdded) {
            (activity as MainActivity).snackError(getString(R.string.cant_fetch_accounts))

            requestCount++

            onItemsLoadComplete()

            e.printStackTrace()
          }
        }
      }
    }
  }

  private fun onComplete(w: ArrayList<TransactionDisplay>, storedwallets: ArrayList<StorableWallet>) {
    addToWallets(w)

    requestCount

    if (requestCount >= storedwallets.size * 2) {
      onItemsLoadComplete()

      // If transaction was send via App and has no confirmations yet (Still show it when users refreshes for 10 minutes)
      if (unconfirmedAddedTime + 10 * 60 * 1000 < System.currentTimeMillis()) { // After 10 minutes remove unconfirmed (should now have at least 1 confirmation anyway)
        unconfirmed = null
      }

      if (unconfirmed != null) {
        if (wallets[0].amount == unconfirmed!!.amount) {
          unconfirmed = null
        } else {
          wallets.add(0, unconfirmed!!)
        }
      }

      nothing_found.visibility = if (wallets.size == 0) View.VISIBLE else GONE
      walletAdapter?.notifyDataSetChanged()
    }
  }

  fun addUnconfirmedTransaction(from: String, to: String, amount: BigInteger) {
    unconfirmed = TransactionDisplay(from, to, amount, 0, System.currentTimeMillis(), "", TransactionDisplay.NORMAL, "", "0", 0, 1, 1, false)
    unconfirmedAddedTime = System.currentTimeMillis()
    wallets.add(0, unconfirmed!!)

    notifyDataSetChanged()
  }

}
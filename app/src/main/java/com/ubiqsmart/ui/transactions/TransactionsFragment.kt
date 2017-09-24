package com.ubiqsmart.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.ubiqsmart.R
import com.ubiqsmart.repository.api.EtherscanAPI
import com.ubiqsmart.repository.data.TransactionDisplay
import com.ubiqsmart.ui.detail.AddressDetailActivity
import com.ubiqsmart.utils.AddressNameConverter
import com.ubiqsmart.utils.RequestCache
import com.ubiqsmart.utils.ResponseParser
import com.ubiqsmart.utils.WalletStorage
import kotlinx.android.synthetic.main.fragment_transaction.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.*

class TransactionsFragment : TransactionsAbstractFragment() {

  override val walletStorage: WalletStorage by with(this).instance()
  override val addressNameConverter: AddressNameConverter by with(this).instance()
  override val etherscanApi: EtherscanAPI by instance()

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = super.onCreateView(inflater, container, savedInstanceState)

    new_transaction.visibility = GONE
    request_transaction?.setVisibility(GONE)
    fab_menu_view.visibility = View.GONE

    return rootView
  }

  override fun update(force: Boolean) {
    if (activity == null) {
      return
    }

    resetRequestCount()
    wallets.clear()

    if (swipe_refresh_layout2 != null) {
      swipe_refresh_layout2!!.isRefreshing = true
    }

    try {
      etherscanApi.getNormalTransactions(address, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          if (isAdded) {
            activity.runOnUiThread {
              onItemsLoadComplete()
              (activity as AddressDetailActivity).snackError(getString(R.string.err_no_con))
            }
          }
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
          val restring = response.body()!!.string()

          if (restring != null && restring.length > 2) {
            RequestCache.getInstance().put(RequestCache.TYPE_TXS_NORMAL, address, restring)
          }

          val w = ArrayList(ResponseParser.parseTransactions(restring, getString(R.string.unnamed_address), address, TransactionDisplay.NORMAL))
          if (isAdded) {
            activity.runOnUiThread { onComplete(w) }
          }
        }
      }, force)

      etherscanApi.getInternalTransactions(address, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          if (isAdded) {
            activity.runOnUiThread {
              onItemsLoadComplete()
              (activity as AddressDetailActivity).snackError(getString(R.string.err_no_con))
            }
          }
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
          val restring = response.body()!!.string()
          if (restring != null && restring.length > 2) RequestCache.getInstance().put(RequestCache.TYPE_TXS_INTERNAL, address, restring)
          val w = ArrayList(
              ResponseParser.parseTransactions(restring, getString(R.string.unnamed_address), address, TransactionDisplay.CONTRACT))
          if (isAdded) {
            activity.runOnUiThread { onComplete(w) }
          }
        }
      }, force)
    } catch (e: IOException) {
      if (activity != null) {
        (activity as AddressDetailActivity).snackError(getString(R.string.cant_fetch_accounts))
      }

      onItemsLoadComplete()

      e.printStackTrace()
    }

  }

  private fun onComplete(w: List<TransactionDisplay>) {
    addToWallets(w)
    addRequestCount()
    if (requestCount >= 2) {
      onItemsLoadComplete()
      nothing_found?.visibility = if (wallets.size == 0) View.VISIBLE else View.GONE
      walletAdapter?.notifyDataSetChanged()
    }
  }

}
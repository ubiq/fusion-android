package com.ubiqsmart.ui.transactions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.ubiqsmart.R
import com.ubiqsmart.repository.api.EtherscanAPI
import com.ubiqsmart.repository.data.TransactionDisplay
import com.ubiqsmart.ui.base.BaseFragment
import com.ubiqsmart.ui.detail.AddressDetailActivity
import com.ubiqsmart.ui.send.RequestEtherActivity
import com.ubiqsmart.ui.send.SendActivity
import com.ubiqsmart.ui.transactions.adapter.TransactionAdapter
import com.ubiqsmart.utils.AddressNameConverter
import com.ubiqsmart.utils.DialogFactory
import com.ubiqsmart.utils.WalletStorage
import kotlinx.android.synthetic.main.fragment_transaction.*
import java.util.*

abstract class TransactionsAbstractFragment : BaseFragment(), View.OnClickListener, View.OnCreateContextMenuListener {

  protected abstract val walletStorage: WalletStorage
  protected abstract val addressNameConverter: AddressNameConverter
  protected abstract val etherscanApi: EtherscanAPI

  protected var walletAdapter: TransactionAdapter? = null

  @get:Synchronized
  @set:Synchronized
  protected var wallets: MutableList<TransactionDisplay> = ArrayList()

  protected var address: String? = null

  @get:Synchronized
  @set:Synchronized
  protected var requestCount = 0

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.fragment_transaction, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    if (arguments != null) {
      address = arguments.getString("ADDRESS")
      (view?.findViewById<View>(R.id.infoText) as TextView).setText(R.string.trans_no_trans_found)
    }

    walletAdapter = TransactionAdapter(activity, wallets, this, this)

    val mgr = LinearLayoutManager(activity)
    val dividerItemDecoration = DividerItemDecoration(activity, mgr.orientation)

    recycler_view.apply {
      layoutManager = mgr
      itemAnimator = DefaultItemAnimator()
      adapter = walletAdapter
      addItemDecoration(dividerItemDecoration)
    }

    swipe_refresh_layout2?.apply {
      setColorSchemeColors(ContextCompat.getColor(context, R.color.primary))
      setOnRefreshListener { update(true) }
    }

    recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        if (address != null) {
          return
        }

        if (dy > 0) {
          fab_menu_view.hideMenu(true)
        } else if (dy < 0) {
          fab_menu_view.showMenu(true)
        }
      }
    })

    request_transaction.setOnClickListener { openRequestActivity() }
    new_transaction.setOnClickListener { openSendActivity() }

    update(false)
    walletAdapter?.notifyDataSetChanged()
  }

  private fun openSendActivity() {
    if (walletStorage.fullOnly.size == 0) {
      DialogFactory.noFullWallet(activity)
    } else {
      val newTrans = Intent(context, SendActivity::class.java)
      if (address != null) {
        newTrans.putExtra("FROM_ADDRESS", address)
      }

      activity.startActivityForResult(newTrans, SendActivity.REQUEST_CODE)
    }
  }

  private fun openRequestActivity() {
    if (walletStorage.get().size == 0) {
      DialogFactory.noWallet(activity)
    } else {
      val newTrans = Intent(context, RequestEtherActivity::class.java)

      activity.startActivity(newTrans)
    }
  }

  fun notifyDataSetChanged() {
    walletAdapter?.notifyDataSetChanged()
  }

  abstract fun update(force: Boolean)

  @Synchronized
  fun addRequestCount() {
    requestCount++
  }

  @Synchronized
  fun resetRequestCount() {
    requestCount = 0
  }

  fun onItemsLoadComplete() {
    if (swipe_refresh_layout2 == null) {
      return
    }
    swipe_refresh_layout2!!.isRefreshing = false
  }

  @Synchronized
  fun addToWallets(w: List<TransactionDisplay>) {
    wallets.addAll(w)
    Collections.sort(wallets) { o1, o2 -> o1.compareTo(o2) }
  }

  override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
    menu.setHeaderTitle(R.string.trans_menu_title)
    menu.add(0, 100, 0, R.string.trans_menu_changename)//groupId, itemId, order, title
    menu.add(0, 101, 0, R.string.trans_menu_viewreceiver)
    menu.add(0, 102, 0, R.string.trans_menu_openinb)
  }

  override fun onContextItemSelected(item: MenuItem?): Boolean {
    val position: Int

    try {
      position = walletAdapter?.position ?: 0
    } catch (e: Exception) {
      e.printStackTrace()
      return super.onContextItemSelected(item)
    }

    when (item?.itemId) {
      100 -> { // Change Address Name
        setName(wallets[position].toAddress)
      }

      101 -> { // Open in AddressDetailActivity
        val i = Intent(activity, AddressDetailActivity::class.java).apply {
          putExtra("ADDRESS", wallets[position].toAddress)
        }

        startActivity(i)
      }

      102 -> { // Open in Browser
        val url = "https://etherscan.io/tx/" + wallets[position].txHash
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)

        startActivity(i)
      }
    }

    return super.onContextItemSelected(item)
  }

  fun setName(address: String) {
    val builder = if (Build.VERSION.SDK_INT >= 24) AlertDialog.Builder(activity, R.style.AlertDialogTheme) else AlertDialog.Builder(activity)
    builder.setTitle(R.string.name_other_address)

    val input = EditText(activity).apply {
      setText(addressNameConverter.get(address))
      inputType = InputType.TYPE_CLASS_TEXT
      setSingleLine()
      setSelection(text.length)
      inputType = InputType.TYPE_CLASS_TEXT
      onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
          val inputMgr = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
          inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
      }
    }

    val activity = activity

    val container = FrameLayout(activity)
    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
      leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
      topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
      bottomMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
      rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    }

    input.layoutParams = params

    container.addView(input)

    builder.apply {
      setView(container)
      setPositiveButton(R.string.button_ok) { dialog, _ ->
        addressNameConverter.put(address, input.text.toString())
        val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMgr.hideSoftInputFromWindow(input.windowToken, 0)
        notifyDataSetChanged()
        dialog.dismiss()
      }
      setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.cancel() }
    }
    builder.show()
  }

  override fun onClick(view: View) {
    if (activity == null) {
      return
    }

    val itemPosition = recycler_view.getChildLayoutPosition(view)
    if (itemPosition >= wallets.size) {
      return
    }

    DialogFactory.showTXDetails(activity, wallets[itemPosition])
  }
}
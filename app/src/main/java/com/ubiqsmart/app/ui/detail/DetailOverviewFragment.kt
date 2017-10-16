package com.ubiqsmart.app.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import com.github.clans.fab.FloatingActionButton
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.ubiqsmart.R
import com.ubiqsmart.app.interfaces.LastIconLoaded
import com.ubiqsmart.datasource.api.EtherscanAPI
import com.ubiqsmart.domain.models.Token
import com.ubiqsmart.domain.models.WatchWallet
import com.ubiqsmart.app.ui.base.BaseFragment
import com.ubiqsmart.app.ui.detail.adapter.TokenAdapter
import com.ubiqsmart.app.ui.main.MainActivity
import com.ubiqsmart.app.ui.send.SendActivity
import com.ubiqsmart.app.utils.*
import kotlinx.android.synthetic.main.fragment_detail_ov.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import java.io.IOException
import java.math.BigDecimal
import java.util.*

class DetailOverviewFragment : BaseFragment(), View.OnClickListener, View.OnCreateContextMenuListener, LastIconLoaded {

  private var ethaddress: String? = ""
  private var type: Byte = 0
  private var balanceDouble = BigDecimal("0")

  private var recyclerView: RecyclerView? = null
  private var walletAdapter: TokenAdapter? = null
  private val token = ArrayList<Token>()
  private var swipeLayout: SwipeRefreshLayout? = null

  private val exchangeCalculator: ExchangeCalculator by instance()
  private val walletStorage: WalletStorage by with(this).instance()
  private val addressNameConverter: AddressNameConverter by with(this).instance()
  private val etherscanApi: EtherscanAPI by instance()

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = inflater!!.inflate(R.layout.fragment_detail_ov, container, false)

    ethaddress = arguments.getString("ADDRESS")
    type = arguments.getByte("TYPE")

    val (name, rate) = exchangeCalculator.current
    balanceDouble = BigDecimal(arguments.getDouble("BALANCE"))
    balance_view.text = exchangeCalculator.convertRateExact(balanceDouble, rate) + ""
    currency_view.text = name

    recyclerView = rootView.findViewById(R.id.recycler_view)
    walletAdapter = TokenAdapter(context, token, this, this)
    val mgr = LinearLayoutManager(activity)
    recyclerView!!.layoutManager = mgr
    recyclerView!!.itemAnimator = DefaultItemAnimator()
    recyclerView!!.adapter = walletAdapter

    val dividerItemDecoration = DividerItemDecoration(recyclerView!!.context, mgr.orientation)
    recyclerView!!.addItemDecoration(dividerItemDecoration)

    swipeLayout = rootView.findViewById(R.id.swipe_refresh_layout2)
    swipeLayout!!.setColorSchemeColors(ContextCompat.getColor(activity, R.color.primary))
    swipeLayout!!.setOnRefreshListener {
      try {
        update(true)
      } catch (e: IOException) {
        if (activity != null) {
          (activity as MainActivity).snackError(getString(R.string.no_internet_connection))
        }
        e.printStackTrace()
      }
    }

    header_view!!.setOnClickListener {
      val (_, _, _) = exchangeCalculator.next()
      balance_view.text = exchangeCalculator.convertRateExact(balanceDouble, rate) + ""
      currency_view.text = name
      walletAdapter?.notifyDataSetChanged()
      if (activity != null) {
        (activity as MainActivity).broadCastDataSetChanged()
      }
    }

    address_image_view!!.setImageBitmap(Blockies.createIcon(ethaddress!!, 24))
    eth_address_view!!.text = ethaddress

    val fab_setName = rootView.findViewById<FloatingActionButton>(R.id.set_name)
    fab_setName.setOnClickListener { setName() }

    val send_ether = rootView.findViewById<FloatingActionButton>(R.id.send_ether) // Send Ether to
    send_ether.setOnClickListener {
      if (walletStorage.fullOnly.size == 0) {
        DialogFactory.noFullWallet(context)
      } else {
        val tx = Intent(activity, SendActivity::class.java)
        tx.putExtra("TO_ADDRESS", ethaddress)
        activity.startActivityForResult(tx, SendActivity.REQUEST_CODE)
      }
    }

    val send_ether_from = rootView.findViewById<FloatingActionButton>(R.id.send_ether_from)
    send_ether_from.setOnClickListener {
      if (walletStorage.fullOnly.size == 0) {
        DialogFactory.noFullWallet(context)
      } else {
        val tx = Intent(activity, SendActivity::class.java)
        tx.putExtra("FROM_ADDRESS", ethaddress)
        activity.startActivityForResult(tx, SendActivity.REQUEST_CODE)
      }
    }

    val fab_add = rootView.findViewById<FloatingActionButton>(R.id.add_as_watch)
    fab_add.setOnClickListener {
      val suc = walletStorage.add(WatchWallet(ethaddress!!))
      Handler().postDelayed({
        if (isAdded) {
          val activity = activity
          if (activity != null) {
            (activity as MainActivity).snackError(
                activity.resources.getString(if (suc) R.string.main_ac_wallet_added_suc else R.string.main_ac_wallet_added_er))
          }
        }
      }, 100)
    }

    if (type == AddressDetailActivity.OWN_WALLET) {
      fab_add.visibility = View.GONE
    }

    if (!walletStorage.isFullWallet(ethaddress)) {
      send_ether_from.visibility = View.GONE
    }

    //if (ac.getAppBar() != null) {
    //  ac.getAppBar().addOnOffsetChangedListener(new AppBarStateChangeListener() {
    //    @Override public void onStateChanged(AppBarLayout appBarLayout, State state) {
    //      if (state == State.COLLAPSED) {
    //        fab_menu_view.hideMenu(true);
    //      } else {
    //        fab_menu_view.showMenu(true);
    //      }
    //    }
    //  });
    //}

    try {
      update(false)
    } catch (e: IOException) {
      e.printStackTrace()
    }

    return rootView
  }

  @Throws(IOException::class)
  fun update(force: Boolean) {
    token.clear()
    balanceDouble = BigDecimal("0")
    etherscanApi.getBalance(ethaddress, object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        activity.runOnUiThread {
          (activity as MainActivity).snackError(getString(R.string.cant_connect_to_network))
          onItemsLoadComplete()
        }
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {
        val ethbal: BigDecimal

        try {
          ethbal = BigDecimal(ResponseParser.parseBalance(response.body()!!.string()))
          token.add(0, Token("Ether", "ETH", ethbal.multiply(BigDecimal(1000.0)), 3, 1.0, "", "", 0, 0))
          balanceDouble = balanceDouble.add(ethbal)
        } catch (e: JSONException) {
          activity.runOnUiThread { onItemsLoadComplete() }
          e.printStackTrace()
        }

        val (name, rate) = exchangeCalculator.current

        activity.runOnUiThread {
          balance_view!!.text = exchangeCalculator.convertRateExact(balanceDouble, rate) + ""
          currency_view!!.text = name
          walletAdapter!!.notifyDataSetChanged()
        }
      }
    })

    etherscanApi.getTokenBalances(ethaddress, object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        activity.runOnUiThread {
          (activity as MainActivity).snackError(getString(R.string.cant_connect_to_network))
          onItemsLoadComplete()
        }
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {
        try {
          val restring = response.body()!!.string()

          token.addAll(ResponseParser.parseTokens(context, restring, this@DetailOverviewFragment))

          balanceDouble = balanceDouble.add(BigDecimal(exchangeCalculator.sumUpTokenEther(token)))

          val (name, rate) = exchangeCalculator.current
          activity.runOnUiThread {
            balance_view!!.text = exchangeCalculator.convertRateExact(balanceDouble, rate) + ""
            currency_view!!.text = name
            walletAdapter!!.notifyDataSetChanged()
            onItemsLoadComplete()
          }
        } catch (e: Exception) {
          activity.runOnUiThread { onItemsLoadComplete() }
        }

      }
    })
  }

  fun setName() {
    val builder = if (Build.VERSION.SDK_INT >= 24) AlertDialog.Builder(activity, R.style.Ubiq_Dialog_Alert) else AlertDialog.Builder(activity)
    if (type == AddressDetailActivity.OWN_WALLET) {
      builder.setTitle(R.string.name_your_address)
    } else {
      builder.setTitle(R.string.name_this_address)
    }

    val activity = activity

    val input = EditText(activity)
    input.setText(addressNameConverter.get(ethaddress!!))
    input.inputType = InputType.TYPE_CLASS_TEXT
    input.setSingleLine()

    val container = FrameLayout(activity)

    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    params.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    input.layoutParams = params
    input.setSelection(input.text.length)

    container.addView(input)
    builder.setView(container)
    input.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
      if (hasFocus) {
        val inputMgr = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
      }
    }
    builder.setPositiveButton(R.string.button_ok) { _, _ ->
      val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMgr.hideSoftInputFromWindow(input.windowToken, 0)
      addressNameConverter.put(ethaddress!!, input.text.toString())
      getActivity().title = input.text.toString()
    }
    builder.setNegativeButton(R.string.button_cancel) { dialog, _ ->
      val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMgr.hideSoftInputFromWindow(input.windowToken, 0)
      dialog.cancel()
    }

    builder.show()
  }

  internal fun onItemsLoadComplete() {
    if (swipeLayout == null) {
      return
    }
    swipeLayout!!.isRefreshing = false
  }

  override fun onClick(view: View) {
    if (activity == null) {
      return
    }

    val itemPosition = recyclerView!!.getChildLayoutPosition(view)
    if (itemPosition == 0 || itemPosition >= token.size) {
      return   // if clicked on Ether
    }

    DialogFactory.showTokenDetails(activity, token[itemPosition])
  }

  override fun onLastIconDownloaded() {
    if (walletAdapter != null && activity != null) {
      activity.runOnUiThread { walletAdapter!!.notifyDataSetChanged() }
    }
  }
}
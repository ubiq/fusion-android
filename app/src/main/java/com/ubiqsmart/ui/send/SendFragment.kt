package com.ubiqsmart.ui.send

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.BuildConfig
import com.ubiqsmart.R
import com.ubiqsmart.repository.api.EtherscanAPI
import com.ubiqsmart.services.TransactionService
import com.ubiqsmart.ui.base.BaseFragment
import com.ubiqsmart.utils.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

class SendFragment : BaseFragment() {

  private var ac: SendActivity? = null
  private var send: Button? = null
  private var amount: EditText? = null
  private var toAddress: TextView? = null
  private var toName: TextView? = null
  private var usdPrice: TextView? = null
  private var gasText: TextView? = null
  private var fromName: TextView? = null
  private var availableEth: TextView? = null
  private var availableFiat: TextView? = null
  private var availableFiatSymbol: TextView? = null
  private var txCost: TextView? = null
  private var txCostFiat: TextView? = null
  private var txCostFiatSymbol: TextView? = null
  private var totalCost: TextView? = null
  private var totalCostFiat: TextView? = null
  private var totalCostFiatSymbol: TextView? = null
  private var gas: SeekBar? = null
  private var toicon: ImageView? = null
  private var fromicon: ImageView? = null
  private var spinner: Spinner? = null
  private var currencySpinner: Spinner? = null
  private var amountInEther = true
  private var gaslimit = BigInteger("21000")
  private var curAvailable = BigDecimal.ZERO
  private var curTxCost = BigDecimal("0.000252")
  private var curAmount = BigDecimal.ZERO
  private var expertMode: LinearLayout? = null
  private var data: EditText? = null
  private var userGasLimit: EditText? = null

  private val exchangeCalculator: ExchangeCalculator by instance()
  private val addressNameConverter: AddressNameConverter by instance()
  private val walletStorage: WalletStorage by instance()
  private val etherscanapi: EtherscanAPI by instance()

  private val curTotalCost: BigDecimal
    get() = curAmount.add(curTxCost)

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = inflater!!.inflate(R.layout.fragment_send, container, false)

    ac = this.activity as SendActivity

    send = rootView.findViewById(R.id.send)
    amount = rootView.findViewById(R.id.amount)
    gas = rootView.findViewById(R.id.seekBar)
    toAddress = rootView.findViewById(R.id.toAddress)
    toName = rootView.findViewById(R.id.toName)
    fromName = rootView.findViewById(R.id.fromName)
    usdPrice = rootView.findViewById(R.id.usdPrice)

    availableEth = rootView.findViewById(R.id.ethAvailable)
    availableFiat = rootView.findViewById(R.id.ethAvailableFiat)
    availableFiatSymbol = rootView.findViewById(R.id.ethAvailableFiatSymbol)

    txCost = rootView.findViewById(R.id.txCost)
    txCostFiat = rootView.findViewById(R.id.txCostFiat)
    txCostFiatSymbol = rootView.findViewById(R.id.txCostFiatSymbol)

    totalCost = rootView.findViewById(R.id.totalCost)
    totalCostFiat = rootView.findViewById(R.id.totalCostFiat)
    totalCostFiatSymbol = rootView.findViewById(R.id.totalCostFiatSymbol)

    gasText = rootView.findViewById(R.id.gasText)
    toicon = rootView.findViewById(R.id.toicon)
    fromicon = rootView.findViewById(R.id.fromicon)
    expertMode = rootView.findViewById(R.id.expertmode)
    data = rootView.findViewById(R.id.data)
    userGasLimit = rootView.findViewById(R.id.gaslimit)

    rootView.findViewById<View>(R.id.expertmodetrigger).setOnClickListener {
      if (expertMode!!.visibility == View.GONE) {
        CollapseAnimator.expand(expertMode)
      } else {
        CollapseAnimator.collapse(expertMode)
      }
    }

    val arguments = arguments

    if (arguments.containsKey("TO_ADDRESS")) {
      setToAddress(arguments.getString("TO_ADDRESS"))
    }

    if (arguments.containsKey("AMOUNT")) {
      curAmount = BigDecimal(arguments.getString("AMOUNT"))
      amount!!.setText(arguments.getString("AMOUNT"))
    }

    gas!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        gasText!!.text = String.format(Locale.getDefault(), "%d", i + 1)
        curTxCost = BigDecimal(gaslimit).multiply(BigDecimal((i + 1).toString() + "")).divide(BigDecimal("1000000000"), 6, BigDecimal.ROUND_DOWN)

        updateDisplays()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}

      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    spinner = rootView.findViewById(R.id.spinner)

    val spinnerArrayAdapter = object : ArrayAdapter<String>(ac!!, R.layout.address_spinner, walletStorage.fullOnly) {
      override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        view.setPadding(0, view.paddingTop, view.paddingRight, view.paddingBottom)
        return view
      }
    }
    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    spinner!!.adapter = spinnerArrayAdapter
    spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        try {
          etherscanapi.getBalance(spinner!!.selectedItem.toString(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
              ac!!.runOnUiThread { ac!!.snackError("Cant fetch your account balance", Snackbar.LENGTH_LONG) }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
              ac!!.runOnUiThread {
                try {
                  curAvailable = BigDecimal(ResponseParser.parseBalance(response.body()!!.string(), 6))
                  updateDisplays()
                } catch (e: Exception) {
                  ac!!.snackError("Cant fetch your account balance")
                  e.printStackTrace()
                }
              }
            }
          })
        } catch (e: IOException) {
          e.printStackTrace()
        }

        fromicon!!.setImageBitmap(Blockies.createIcon(spinner!!.selectedItem.toString().toLowerCase()))
        fromName!!.text = addressNameConverter.get(spinner!!.selectedItem.toString().toLowerCase())
      }

      override fun onNothingSelected(adapterView: AdapterView<*>) {}
    }

    amount!!.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable) {}

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        updateAmount(s.toString())
        updateDisplays()
      }
    })

    currencySpinner = rootView.findViewById(R.id.currency_spinner)

    val currencyList = ArrayList<String>()
    currencyList.add(getString(R.string.currency_eth))
    currencyList.add(exchangeCalculator.mainCurreny.name ?: "")

    val curAdapter = ArrayAdapter(ac!!, android.R.layout.simple_spinner_item, currencyList)
    curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    currencySpinner!!.adapter = curAdapter
    currencySpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        amountInEther = i == 0

        updateAmount(amount!!.text.toString())
        updateDisplays()
      }

      override fun onNothingSelected(adapterView: AdapterView<*>) {}
    }

    send!!.setOnClickListener(View.OnClickListener {
      if ((amount!!.text.isEmpty() || BigDecimal(amount!!.text.toString()).compareTo(BigDecimal("0")) <= 0) && data!!.text.isEmpty()) {
        ac!!.snackError(getString(R.string.err_send_noamount))
        return@OnClickListener
      }
      if (toAddress == null || toAddress!!.text.length == 0) {
        ac!!.snackError(getString(R.string.err_send_noreceiver))
        return@OnClickListener
      }
      if (spinner == null || spinner!!.selectedItem == null) return@OnClickListener
      try {
        if (BuildConfig.DEBUG) {
          Log.d("etherbalance",
              String.format("%s | %s | %s | %s | %s", curTotalCost.compareTo(curAvailable) < 0, curTotalCost, curAvailable, data!!.text,
                  curAmount))
        }
        if (curTotalCost.compareTo(curAvailable) < 0 || BuildConfig.DEBUG || data!!.text.length > 0) {
          askForPasswordAndDecode(spinner!!.selectedItem.toString())
        } else {
          ac!!.snackError(getString(R.string.err_send_not_enough_ether))
        }
      } catch (e: Exception) {
        ac!!.snackError(getString(R.string.err_send_invalidamount))
      }
    })

    if (arguments.containsKey("FROM_ADDRESS")) {
      setFromAddress(arguments.getString("FROM_ADDRESS"))
    } else {
      spinner!!.setSelection(0)
    }

    updateDisplays()

    return rootView
  }

  private fun setFromAddress(from: String?) {
    val walletStorage = WalletStorage.getInstance(activity, null)
    val fullwallets = walletStorage.fullOnly
    for (i in fullwallets.indices) {
      if (fullwallets[i].equals(from!!, ignoreCase = true)) {
        spinner!!.setSelection(i)
      }
    }
  }

  private fun updateDisplays() {
    updateAvailableDisplay()
    updateAmountDisplay()
    updateTxCostDisplay()
    updateTotalCostDisplay()
  }

  private fun updateAvailableDisplay() {
    exchangeCalculator.index = 2

    availableEth!!.text = curAvailable.toString()
    availableFiat!!.text = exchangeCalculator.convertRateExact(curAvailable, exchangeCalculator.usdPrice)
    availableFiatSymbol!!.text = exchangeCalculator.current.shorty
  }

  private fun updateAmount(str: String) {
    curAmount = try {
      val origA = BigDecimal(str)
      if (amountInEther) origA else origA.divide(BigDecimal(exchangeCalculator.usdPrice), 7, RoundingMode.FLOOR)
    } catch (e: NumberFormatException) {
      BigDecimal.ZERO
    }
  }

  private fun updateAmountDisplay() {
    val price: String
    if (amountInEther) {
      price = exchangeCalculator.convertRateExact(curAmount, exchangeCalculator.usdPrice) + " " + exchangeCalculator.mainCurreny.name
    } else {
      exchangeCalculator.index = 0
      price = curAmount.toPlainString() + " " + exchangeCalculator.current.shorty
    }

    usdPrice!!.text = price
  }

  private fun updateTxCostDisplay() {
    exchangeCalculator.index = 2

    txCost!!.text = curTxCost.toString()
    txCostFiat!!.text = exchangeCalculator.convertRateExact(curTxCost, exchangeCalculator.usdPrice)
    txCostFiatSymbol!!.text = exchangeCalculator.current.shorty
  }

  private fun updateTotalCostDisplay() {
    exchangeCalculator.index = 2

    val curTotalCost = curTotalCost

    totalCost!!.text = curTotalCost.toString()
    totalCostFiat!!.text = exchangeCalculator.convertRateExact(curTotalCost, exchangeCalculator.usdPrice)
    totalCostFiatSymbol!!.text = exchangeCalculator.current.shorty
  }

  private fun getEstimatedGasPriceLimit() {
    try {
      EtherscanAPI.getInstance().getGasLimitEstimate(toAddress!!.text.toString(), object : Callback {
        override fun onFailure(call: Call, e: IOException) {}

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
          try {
            gaslimit = ResponseParser.parseGasPrice(response.body()!!.string())
            ac!!.runOnUiThread { userGasLimit!!.setText(String.format("%s", gaslimit)) }
          } catch (e: Exception) {
            e.printStackTrace()
          }

        }
      })
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  private fun askForPasswordAndDecode(fromAddress: String) {
    val builder = AlertDialog.Builder(ac!!, R.style.AlertDialogTheme)
    builder.setTitle(R.string.wallet_password)

    val input = EditText(ac)
    val showpw = CheckBox(ac)
    showpw.setText(R.string.password_in_clear_text)
    input.transformationMethod = PasswordTransformationMethod.getInstance()

    val container = LinearLayout(ac)
    container.orientation = LinearLayout.VERTICAL
    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    params.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)

    val params2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    params2.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    params2.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
    input.layoutParams = params
    showpw.layoutParams = params2

    container.addView(input)
    container.addView(showpw)
    builder.setView(container)

    showpw.setOnCheckedChangeListener { buttonView, isChecked ->
      if (!isChecked) {
        input.transformationMethod = PasswordTransformationMethod.getInstance()
      } else {
        input.transformationMethod = HideReturnsTransformationMethod.getInstance()
      }
      input.setSelection(input.text.length)
    }

    builder.setView(container)
    input.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
      if (hasFocus) {
        val inputMgr = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
      }
    }
    builder.setPositiveButton(getString(android.R.string.ok)) { dialog, which ->
      val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMgr.hideSoftInputFromWindow(input.windowToken, 0)
      sendEther(input.text.toString(), fromAddress)
      dialog.dismiss()
    }
    builder.setNegativeButton(getString(android.R.string.cancel)) { dialog, which ->
      val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMgr.hideSoftInputFromWindow(input.windowToken, 0)
      dialog.cancel()
    }

    builder.show()

  }

  private fun sendEther(password: String, fromAddress: String) {
    val txService = Intent(ac, TransactionService::class.java)
    txService.putExtra("FROM_ADDRESS", fromAddress)
    txService.putExtra("TO_ADDRESS", toAddress!!.text.toString())
    txService.putExtra("AMOUNT", curAmount.toPlainString()) // In ether, gets converted by the service itself
    txService.putExtra("GAS_PRICE", BigDecimal((gas!!.progress + 1).toString() + "").multiply(BigDecimal("1000000000")).toPlainString())// "21000000000");
    txService.putExtra("GAS_LIMIT", if (userGasLimit!!.text.length <= 0) gaslimit.toString() else userGasLimit!!.text.toString())
    txService.putExtra("PASSWORD", password)
    txService.putExtra("DATA", data!!.text.toString())
    ac!!.startService(txService)

    val data = Intent()
    data.putExtra("FROM_ADDRESS", fromAddress)
    data.putExtra("TO_ADDRESS", toAddress!!.text.toString())
    data.putExtra("AMOUNT", curAmount.toPlainString())

    ac!!.setResult(RESULT_OK, data)
    ac!!.finish()
  }

  fun setToAddress(to: String?) {
    if (toAddress == null) {
      return
    }
    toAddress!!.text = to
    val name = addressNameConverter.get(to!!)
    toName!!.text = name ?: to.substring(0, 10)
    toicon!!.setImageBitmap(Blockies.createIcon(to.toLowerCase()))
    getEstimatedGasPriceLimit()
  }
}
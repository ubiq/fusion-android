package com.ubiqsmart.app.ui.send

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.*
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.ubiqsmart.R
import com.ubiqsmart.R.id.qrcode
import com.ubiqsmart.app.ui.base.SecureActivity
import com.ubiqsmart.app.ui.main.adapter.WalletAdapter
import com.ubiqsmart.app.utils.AddressNameConverter
import com.ubiqsmart.app.utils.ExchangeCalculator
import com.ubiqsmart.app.utils.WalletStorage
import com.ubiqsmart.app.utils.qr.AddressEncoder
import com.ubiqsmart.app.utils.qr.Contents
import com.ubiqsmart.app.utils.qr.QREncoder
import com.ubiqsmart.domain.models.WalletEntry
import java.math.BigDecimal
import java.util.*

class RequestEtherActivity : SecureActivity(), View.OnClickListener {

  private var coord: CoordinatorLayout? = null
  private var qr: ImageView? = null
  private var recyclerView: RecyclerView? = null
  private var walletAdapter: WalletAdapter? = null
  private val wallets = ArrayList<WalletEntry>()
  private var selectedEtherAddress: String? = null
  private var amount: TextView? = null
  private var usdPrice: TextView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_request_ether)

    val toolbar = findViewById<Toolbar>(R.id.toolbar_view)
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayShowTitleEnabled(false)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    coord = findViewById(R.id.main_content)
    qr = findViewById(qrcode)
    recyclerView = findViewById(R.id.recycler_view)
    amount = findViewById(R.id.amount)
    usdPrice = findViewById(R.id.usdPrice)
    walletAdapter = WalletAdapter(this, wallets, this, this)

    val mgr = LinearLayoutManager(this)
    recyclerView!!.layoutManager = mgr
    recyclerView!!.itemAnimator = DefaultItemAnimator()
    recyclerView!!.adapter = walletAdapter

    val dividerItemDecoration = DividerItemDecoration(recyclerView!!.context, mgr.orientation)
    recyclerView!!.addItemDecoration(dividerItemDecoration)

    amount!!.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.length != 0) {
          try {
            val amountd = java.lang.Double.parseDouble(amount!!.text.toString())
            usdPrice!!.text = String.format("%s %s", ExchangeCalculator.getInstance().displayUsdNicely(ExchangeCalculator.getInstance().convertToUsd(amountd)),
                ExchangeCalculator.getInstance().mainCurreny.name)
            updateQR()
          } catch (e: Exception) {
            e.printStackTrace()
          }

        }
      }
    })

    update()
    updateQR()
  }

  fun update() {
    wallets.clear()

    val myAddresses = ArrayList<WalletEntry>()
    val walletStorage = WalletStorage.getInstance(this, null)

    val storedAddresses = ArrayList(walletStorage.get())
    for (i in storedAddresses.indices) {
      if (i == 0) selectedEtherAddress = storedAddresses[i].pubKey
      myAddresses.add(WalletEntry(AddressNameConverter.getInstance(this).get(storedAddresses[i].pubKey), storedAddresses[i].pubKey))
    }

    wallets.addAll(myAddresses)
    walletAdapter!!.notifyDataSetChanged()
  }

  fun snackError(s: String) {
    if (coord == null) {
      return
    }
    val mySnackbar = Snackbar.make(coord!!, s, Snackbar.LENGTH_SHORT)
    mySnackbar.show()
  }

  fun updateQR() {
    val qrCodeDimention = 400
    var iban = "iban:" + selectedEtherAddress!!
    if (amount!!.text.toString().isNotEmpty() && BigDecimal(amount!!.text.toString()) > BigDecimal("0")) {
      iban += "?amount=" + amount!!.text.toString()
    }

    val qrCodeEncoder: QREncoder
    val temp = AddressEncoder(selectedEtherAddress)
    if (amount!!.text.toString().isNotEmpty() && BigDecimal(amount!!.text.toString()) > BigDecimal("0")) {
      temp.amount = amount!!.text.toString()
    }
    qrCodeEncoder = QREncoder(AddressEncoder.encodeERC(temp), null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention)

    try {
      val bitmap = qrCodeEncoder.encodeAsBitmap()
      qr!!.setImageBitmap(bitmap)
    } catch (e: WriterException) {
      e.printStackTrace()
    }

  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  override fun onClick(view: View) {
    val itemPosition = recyclerView!!.getChildLayoutPosition(view)
    selectedEtherAddress = wallets[itemPosition].publicKey
    updateQR()
  }
}


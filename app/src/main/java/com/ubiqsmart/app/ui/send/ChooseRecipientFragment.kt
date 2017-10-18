package com.ubiqsmart.app.ui.send

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.main.adapter.WalletAdapter
import com.ubiqsmart.app.ui.scanqr.QRScanActivity
import com.ubiqsmart.app.utils.AddressNameConverter
import java.util.*

class ChooseRecipientFragment : Fragment(), View.OnClickListener, View.OnCreateContextMenuListener {

  private var recyclerView: RecyclerView? = null
  private var walletAdapter: WalletAdapter? = null

  private var qr: ImageButton? = null
  private var send: Button? = null
  private var addressBox: EditText? = null

  private val wallets = ArrayList<com.ubiqsmart.domain.models.WalletAdapter>()

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = inflater!!.inflate(R.layout.fragment_recipient, container, false)

    qr = rootView.findViewById(R.id.scan_button)
    send = rootView.findViewById(R.id.send)
    addressBox = rootView.findViewById(R.id.receiver)
    recyclerView = rootView.findViewById(R.id.recycler_view)

    walletAdapter = WalletAdapter(wallets, activity, this, this)

    val mgr = LinearLayoutManager(activity)
    recyclerView!!.layoutManager = mgr
    recyclerView!!.itemAnimator = DefaultItemAnimator()
    recyclerView!!.adapter = walletAdapter

    val dividerItemDecoration = DividerItemDecoration(activity, mgr.orientation)
    recyclerView!!.addItemDecoration(dividerItemDecoration)

    qr!!.setOnClickListener {
      val qr = Intent(activity, QRScanActivity::class.java)
      qr.putExtra("TYPE", QRScanActivity.SCAN_ONLY)

      activity.startActivityForResult(qr, QRScanActivity.REQUEST_CODE)
    }

    send!!.setOnClickListener {
      if (addressBox!!.text.toString().length > 15 && addressBox!!.text.toString().startsWith("0x")) {
        (activity as SendActivity).nextStage(addressBox!!.text.toString())
      } else {
        (activity as SendActivity).snackError(getString(R.string.invalid_recipient))
      }
    }

    update()

    return rootView
  }

  override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
    menu.setHeaderTitle(R.string.addressbook_menu_title)
    menu.add(0, 400, 0, R.string.addressbook_menu_remove)
  }

  override fun onContextItemSelected(item: MenuItem?): Boolean {
    val position: Int
    try {
      position = walletAdapter!!.position
    } catch (e: Exception) {
      e.printStackTrace()
      return super.onContextItemSelected(item)
    }

    when (item!!.itemId) {
      400 -> { // Remove
        AddressNameConverter.getInstance(activity).put(wallets[position].publicKey, null)
        wallets.removeAt(position)
        if (walletAdapter != null) walletAdapter!!.notifyDataSetChanged()
      }
    }
    return super.onContextItemSelected(item)
  }

  fun setRecipientAddress(address: String) {
    if (addressBox == null) return
    addressBox!!.setText(address)
  }

  fun update() {
    if (activity == null) {
      return
    }
    wallets.clear()

    wallets.addAll(ArrayList(AddressNameConverter.getInstance(activity).asAddressBook))
    walletAdapter!!.notifyDataSetChanged()
  }

  override fun onClick(view: View) {
    val itemPosition = recyclerView!!.getChildLayoutPosition(view)
    addressBox!!.setText(wallets[itemPosition].publicKey)
  }
}
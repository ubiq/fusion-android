package com.ubiqsmart.app.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseFragment
import com.ubiqsmart.app.utils.qr.AddressEncoder
import com.ubiqsmart.app.utils.qr.Contents
import com.ubiqsmart.app.utils.qr.QREncoder

class DetailShareFragment : BaseFragment() {

  private var ethaddress: String? = ""

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = inflater!!.inflate(R.layout.fragment_detail_share, container, false)

    ethaddress = arguments.getString("ADDRESS")

    val clipboard = rootView.findViewById<Button>(R.id.copytoclip)
    clipboard.setOnClickListener {
      val i = Intent(Intent.ACTION_SEND)
      i.type = "text/plain"
      i.putExtra(Intent.EXTRA_TEXT, ethaddress)
      startActivity(Intent.createChooser(i, "Share via"))
    }

    val scale = context.resources.displayMetrics.density
    val qrCodeDimention = (310 * scale + 0.5f).toInt()

    val qrcode = rootView.findViewById<ImageView>(R.id.qrcode)

    qrcode.setOnClickListener {
      val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipboardManager.primaryClip = ClipData.newPlainText("", ethaddress)
      Toast.makeText(activity, R.string.wallet_menu_action_copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    val qrCodeEncoder = QREncoder(AddressEncoder.encodeERC(AddressEncoder(ethaddress)), null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention)
    try {
      val bitmap = qrCodeEncoder.encodeAsBitmap()
      qrcode.setImageBitmap(bitmap)
    } catch (e: WriterException) {
      e.printStackTrace()
    }

    return rootView
  }

}
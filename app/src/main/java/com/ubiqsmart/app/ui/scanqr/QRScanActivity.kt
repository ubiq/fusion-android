package com.ubiqsmart.app.ui.scanqr

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseActivity
import com.ubiqsmart.app.utils.qr.AddressEncoder
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.IOException
import java.util.*

class QRScanActivity : BaseActivity(), ZXingScannerView.ResultHandler {

  private var type: Byte = 0

  private var scannerView: ZXingScannerView? = null
  private var barCode: FrameLayout? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_qrscan)

    val toolbar = findViewById<Toolbar>(R.id.toolbar_view)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val title = findViewById<TextView>(R.id.toolbar_title)
    type = intent.getByteExtra("TYPE", SCAN_ONLY)
    title.text = if (type == SCAN_ONLY) getString(R.string.scan_address) else getString(R.string.add_wallet)

    barCode = findViewById(R.id.barcode)

    if (hasPermission(this)) {
      initQRScan(barCode)
    } else {
      askForPermissionRead(this)
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  fun initQRScan(frame: FrameLayout?) {
    scannerView = ZXingScannerView(this)
    frame!!.addView(scannerView)
    scannerView!!.setResultHandler(this)

    val supported = ArrayList<BarcodeFormat>()
    supported.add(BarcodeFormat.QR_CODE)
    scannerView!!.setFormats(supported)
    scannerView!!.startCamera()
  }

  public override fun onPause() {
    super.onPause()
    if (scannerView != null) {
      scannerView!!.stopCamera()
    }
  }

  private fun hasPermission(c: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || c.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
      REQUEST_CAMERA_PERMISSION -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          initQRScan(barCode)
        } else {
          Toast.makeText(this, R.string.grant_camera_permission, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  override fun handleResult(result: Result?) {
    if (result == null) {
      return
    }

    val address = result.text
    try {
      val scanned = AddressEncoder.decode(address)
      val data = Intent()
      data.putExtra("ADDRESS", scanned.address.toLowerCase())

      if (address.length > 42 && !address.startsWith("0x") && scanned.amount == null) {
        type = PRIVATE_KEY
      }

      if (scanned.amount != null) {
        data.putExtra("AMOUNT", scanned.amount)
        type = REQUEST_PAYMENT
      }

      data.putExtra("TYPE", type)
      setResult(Activity.RESULT_OK, data)
      finish()
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  companion object {

    val REQUEST_CODE = 100
    val REQUEST_CAMERA_PERMISSION = 106

    val SCAN_ONLY: Byte = 0
    val ADD_TO_WALLETS: Byte = 1
    val REQUEST_PAYMENT: Byte = 2
    val PRIVATE_KEY: Byte = 3

    fun askForPermissionRead(c: Activity) {
      if (Build.VERSION.SDK_INT < 23) {
        return
      }
      ActivityCompat.requestPermissions(c, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }
  }

}
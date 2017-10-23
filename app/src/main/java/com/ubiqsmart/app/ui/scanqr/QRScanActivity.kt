package com.ubiqsmart.app.ui.scanqr

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseActivity
import com.ubiqsmart.app.utils.qr.AddressEncoder
import kotlinx.android.synthetic.main.activity_qrscan.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.IOException

class QRScanActivity : BaseActivity(), ZXingScannerView.ResultHandler {

  private var type: Byte = 0

  private lateinit var scannerView: ZXingScannerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_qrscan)

    if (hasCameraPermission()) {
      initQRScan()
    } else {
      requestCameraPermission(this)
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  public override fun onPause() {
    scannerView.stopCamera()
    super.onPause()
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
      REQUEST_CAMERA_PERMISSION -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          initQRScan()
        } else {
          Toast.makeText(this, R.string.grant_camera_permission, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  private fun initQRScan() {
    scannerView = ZXingScannerView(this).apply {
      setResultHandler(this@QRScanActivity)
      setFormats(arrayListOf(BarcodeFormat.QR_CODE))
    }

    barcode_container.addView(scannerView)

    scannerView.startCamera()
  }

  private fun hasCameraPermission(): Boolean {
    return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
  }

  override fun handleResult(result: Result?) {
    if (result == null) {
      return
    }

    val address = result.text
    try {
      val scanned = AddressEncoder.decode(address)

      val data = Intent().apply {
        putExtra("ADDRESS", scanned.address.toLowerCase())
        scanned.amount?.let {
          putExtra("AMOUNT", it)
        }
      }

      TODO("Finish converting this piece of code")
//      if (address.length > 42 && !address.startsWith("0x") && scanned.amount == null) {
//        type = PRIVATE_KEY
//      }
//
//      if (scanned.amount != null) {
//        data.putExtra("AMOUNT", scanned.amount)
//        type = REQUEST_PAYMENT
//      }
//
//      data.putExtra("TYPE", type)
//      setResult(Activity.RESULT_OK, data)

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

    fun requestCameraPermission(c: Activity) {
      if (Build.VERSION.SDK_INT < 23) {
        return
      }
      ActivityCompat.requestPermissions(c, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    fun getStartIntent(context: Context) = Intent(context, QRScanActivity::class.java)
  }

}
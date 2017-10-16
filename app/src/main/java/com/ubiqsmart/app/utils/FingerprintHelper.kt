package com.ubiqsmart.app.utils

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import com.ubiqsmart.app.interfaces.FingerprintListener

@RequiresApi(api = Build.VERSION_CODES.M)
class FingerprintHelper(private val listener: FingerprintListener) : FingerprintManager.AuthenticationCallback() {

  private var cancellationSignal: CancellationSignal? = null

  fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
    cancellationSignal = CancellationSignal()

    try {
      manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    } catch (ex: SecurityException) {
      listener.authenticationFailed("An error occurred:\n" + ex.message)
    } catch (ex: Exception) {
      listener.authenticationFailed("An error occurred\n" + ex.message)
    }
  }

  fun cancel() {
    if (cancellationSignal != null) {
      cancellationSignal!!.cancel()
    }
  }

  override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
    listener.authenticationFailed(errString.toString())
  }

  override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
    listener.authenticationFailed(helpString.toString())
  }

  override fun onAuthenticationFailed() {
    listener.authenticationFailed("Authentication failed")
  }

  override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
    listener.authenticationSucceeded(result)
  }

}

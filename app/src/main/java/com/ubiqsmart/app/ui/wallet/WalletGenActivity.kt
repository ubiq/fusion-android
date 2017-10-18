package com.ubiqsmart.app.ui.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.SecureActivity
import com.ubiqsmart.app.utils.Settings

class WalletGenActivity : SecureActivity() {

  private var password: EditText? = null
  private var passwordConfirm: EditText? = null
  private var coord: CoordinatorLayout? = null
  private var walletGenText: TextView? = null
  private var toolbar_title: TextView? = null
  private var privateKeyProvided: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_wallet_gen)

    password = findViewById(R.id.password)
    passwordConfirm = findViewById(R.id.passwordConfirm)
    walletGenText = findViewById(R.id.walletGenText)
    toolbar_title = findViewById(R.id.toolbar_title)

    coord = findViewById(R.id.main_content)

    val emailSignInButton = findViewById<Button>(R.id.email_sign_in_button)
    emailSignInButton.setOnClickListener { gen() }

    if (intent.hasExtra("PRIVATE_KEY")) {
      privateKeyProvided = intent.getStringExtra("PRIVATE_KEY")
      walletGenText!!.text = resources.getText(R.string.import_text)
      toolbar_title!!.setText(R.string.import_title)
      emailSignInButton.setText(R.string.import_button)
    }
  }

  private fun gen() {
    if (passwordConfirm!!.text.toString() != password!!.text.toString()) {
      snackError(resources.getString(R.string.error_incorrect_password))
      return
    }

    if (!isPasswordValid(passwordConfirm!!.text.toString())) {
      snackError(resources.getString(R.string.error_invalid_password))
      return
    }

    Settings.walletBeingGenerated = true // Lock so a user can only generate one wallet at a time

    val data = Intent()
    data.putExtra("PASSWORD", passwordConfirm!!.text.toString())
    if (privateKeyProvided != null) {
      data.putExtra("PRIVATE_KEY", privateKeyProvided)
    }
    setResult(Activity.RESULT_OK, data)
    finish()
  }

  fun snackError(s: String) {
    if (coord == null) return
    val mySnackbar = Snackbar.make(coord!!, s, Snackbar.LENGTH_SHORT)
    mySnackbar.show()
  }

  private fun isPasswordValid(password: String): Boolean {
    return password.length >= 9
  }

  companion object {

    val REQUEST_CODE = 401

    fun getStartIntent(context: Context) = Intent(context, WalletGenActivity::class.java)
  }

}


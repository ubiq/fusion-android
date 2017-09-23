package com.ubiqsmart.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.ubiqsmart.ui.base.BaseActivity
import com.ubiqsmart.ui.main.MainActivity

class SplashActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Handler().postDelayed({
      finish()
      startActivity(Intent(this, MainActivity::class.java))
    }, 2000)
  }

}

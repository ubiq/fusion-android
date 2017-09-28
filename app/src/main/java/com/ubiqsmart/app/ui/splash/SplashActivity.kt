package com.ubiqsmart.app.ui.splash

import android.os.Bundle
import com.ubiqsmart.app.ui.base.BaseActivity
import com.ubiqsmart.app.ui.main.MainActivity
import com.ubiqsmart.app.ui.onboarding.OnBoardingActivity
import com.ubiqsmart.extensions.obtainViewModel

class SplashActivity : BaseActivity(), SplashNavigator {

  private lateinit var viewModel: SplashViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = obtainViewModel(SplashViewModel::class.java)
  }

  override fun openMainScreen() {
    val intent = MainActivity.getStartIntent(this@SplashActivity)
    startActivity(intent)
  }

  override fun openOnBoardingScreen() {
    val intent = OnBoardingActivity.getStartIntent(this@SplashActivity)
    startActivity(intent)
  }

}

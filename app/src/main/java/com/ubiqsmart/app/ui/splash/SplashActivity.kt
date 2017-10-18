package com.ubiqsmart.app.ui.splash

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.widget.Toast
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseActivity
import com.ubiqsmart.app.ui.main.MainActivity2
import com.ubiqsmart.app.ui.onboarding.OnBoardingActivity
import com.ubiqsmart.extensions.obtainViewModel

class SplashActivity : BaseActivity(), SplashNavigator {

  private lateinit var viewModel: SplashViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    onCreateViewModel()
    viewModel.onViewCreated()
  }

  override fun onDestroy() {
    viewModel.onDestroyView()
    super.onDestroy()
  }

  private fun onCreateViewModel() {
    viewModel = obtainViewModel(SplashViewModel::class.java)
    onSubscribeToNavigationChanges()
  }

  private fun onSubscribeToNavigationChanges() {
    val activity = this
    viewModel.run {
      onNavigateToCommand.observe(activity, Observer {
        when (it) {
          SplashNavigator.ONBOARDING_SCREEN -> openOnBoardingScreen()
          else -> openMainScreen()
        }
      })
      onErrorCommand.observe(activity, Observer {
        displayError()
      })
    }
  }

  override fun openMainScreen() {
    val intent = MainActivity2.getStartIntent(this@SplashActivity)
    startActivity(intent)
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
  }

  override fun openOnBoardingScreen() {
    val intent = OnBoardingActivity.getStartIntent(this@SplashActivity)
    startActivity(intent)
  }

  private fun displayError() {
    Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
    finish()
  }

}

package com.ubiqsmart.app.ui.onboarding

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.widget.Toast
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.main.MainActivity2
import com.ubiqsmart.app.ui.onboarding.steps.TOSFragment
import com.ubiqsmart.extensions.getColorCompat
import com.ubiqsmart.extensions.obtainViewModel

class OnBoardingActivity : AppIntro(), OnBoardingNavigator {

  private lateinit var viewModel: OnBoardingViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    onCreateViewModel()
    onCreateIntroSlides()

    viewModel.onViewCreated()
  }

  override fun onDestroy() {
    viewModel.onDestroyView()
    super.onDestroy()
  }

  private fun onCreateViewModel() {
    viewModel = obtainViewModel(OnBoardingViewModel::class.java)
    onSubscribeToNavigationChanges()
  }

  private fun onSubscribeToNavigationChanges() {
    val activity = this
    viewModel.run {
      onNavigateToCommand.observe(activity, Observer {
        openMainScreen()
      })
      onErrorCommand.observe(activity, Observer {
        displayError()
      })
    }
  }

  private fun onCreateIntroSlides() {
    onCreateSplashSlide()
    onCreateWalletSlide()
    onCreateScanQRSlide()
    onCreatePriceWatcherSlide()
    onCreateTOSSlide()

    setSeparatorColor(getColorCompat(android.R.color.transparent))
    isProgressButtonEnabled = true
    skipButtonEnabled = true
  }

  private fun onCreateSplashSlide() {
    val introFragment = AppIntroFragment.newInstance(
        getString(R.string.app_intro_title),
        null,
        getString(R.string.app_intro_text),
        null,
        R.drawable.as_ubiq_splash_background,
        getColorCompat(R.color.background_super_dark),
        0,
        0
    )
    addSlide(introFragment)
  }

  private fun onCreateWalletSlide() {
    val introFragment = AppIntroFragment.newInstance(
        getString(R.string.app_intro_wallet_title),
        null,
        getString(R.string.app_intro_wallet_description),
        null,
        R.drawable.as_ubiq_splash_background,
        getColorCompat(R.color.background_super_dark),
        0,
        0
    )
    addSlide(introFragment)
  }

  private fun onCreateScanQRSlide() {
    val introFragment = AppIntroFragment.newInstance(
        getString(R.string.app_intro_scan_qr_title),
        null,
        getString(R.string.app_intro_scan_qr_description),
        null,
        R.drawable.as_ubiq_splash_background,
        getColorCompat(R.color.background_super_dark),
        0,
        0
    )
    addSlide(introFragment)
  }

  private fun onCreatePriceWatcherSlide() {
    val introFragment = AppIntroFragment.newInstance(
        getString(R.string.app_intro_price_watcher_title),
        null,
        getString(R.string.app_intro_price_watcher_description),
        null,
        R.drawable.as_ubiq_splash_background,
        getColorCompat(R.color.background_super_dark),
        0,
        0
    )
    addSlide(introFragment)
  }

  private fun onCreateTOSSlide() {
    val tosFragment = TOSFragment()
    tosFragment.setOnTosButtonClickListener { _, toggled -> isProgressButtonEnabled = toggled }
    addSlide(tosFragment)
  }

  override fun onSkipPressed(currentFragment: Fragment?) {
    findViewById<ViewPager>(R.id.view_pager).currentItem = 4 // TOS
  }

  override fun onDonePressed(currentFragment: Fragment?) {
    viewModel.saveMarkOnBoardingAsPassed()
  }

  override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
    when (newFragment) {
      is TOSFragment -> {
        val isChecked = newFragment.isTOSButtonChecked()
        isProgressButtonEnabled = isChecked
        skipButtonEnabled = false
      }
      else -> {
        isProgressButtonEnabled = true
        skipButtonEnabled = true
      }
    }
  }

  override fun openMainScreen() {
    val intent = MainActivity2.getStartIntent(this@OnBoardingActivity)
    startActivity(intent)
    finish()
  }

  private fun displayError() {
    Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
    finish()
  }

  companion object {

    fun getStartIntent(context: Context) = Intent(context, OnBoardingActivity::class.java)
  }

}
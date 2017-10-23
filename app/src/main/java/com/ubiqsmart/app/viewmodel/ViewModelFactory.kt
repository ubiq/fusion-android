package com.ubiqsmart.app.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.ubiqsmart.app.ui.main.MainViewModel
import com.ubiqsmart.app.ui.main.fragments.price.PriceViewModel
import com.ubiqsmart.app.ui.main.fragments.wallets.WalletsViewModel
import com.ubiqsmart.app.ui.onboarding.OnBoardingViewModel
import com.ubiqsmart.app.ui.splash.SplashViewModel

class ViewModelFactory private constructor(
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
    when {
      isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(application)
      isAssignableFrom(OnBoardingViewModel::class.java) -> OnBoardingViewModel(application)
      isAssignableFrom(MainViewModel::class.java) -> MainViewModel(application)
      isAssignableFrom(PriceViewModel::class.java) -> PriceViewModel(application)
      isAssignableFrom(WalletsViewModel::class.java) -> WalletsViewModel(application)
      else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  } as T

  companion object {

    @SuppressLint("StaticFieldLeak")
    @Volatile private var INSTANCE: ViewModelFactory? = null

    fun getInstance(application: Application) =
        INSTANCE ?: synchronized(ViewModelFactory::class.java) {
          INSTANCE ?: ViewModelFactory(application).also { INSTANCE = it }
        }
  }
}

package com.ubiqsmart.app.ui.splash

interface SplashNavigator {

  fun openMainScreen()

  fun openOnBoardingScreen()

  companion object {
    const val ONBOARDING_SCREEN = 0
    const val MAIN_SCREEN = 1
  }

}
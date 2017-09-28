package com.ubiqsmart.app.ui.splash

import android.app.Application
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.app.ui.base.BaseViewModel
import com.ubiqsmart.domain.interactors.splash.GetAppStateInteractor
import com.ubiqsmart.domain.models.AppState
import com.ubiqsmart.lifecycle.SingleLiveEvent
import com.ubiqsmart.rx.Schedulers
import java.util.concurrent.TimeUnit

class SplashViewModel(
    app: Application,
    appKodein: Kodein
) : BaseViewModel(app), KodeinAware {

  override val kodein = Kodein {
    extend(appKodein)
    import(SplashDI.Module)
  }

  private val getAppStateInteractor: GetAppStateInteractor = instance()

  private val navigateToCommand = SingleLiveEvent<Int>()

  override fun onViewCreated() {
    getAppState()
  }

  private fun getAppState() {
    val subscription = getAppStateInteractor.getAppState()
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.ui())
        .delay(1, TimeUnit.SECONDS)
        .subscribe({ appState: AppState?, throwable: Throwable? ->
          appState?.let {
            onNavigateTo(it)
          }
          throwable?.let { }
        })

    disposables.add(subscription)
  }

  private fun onNavigateTo(appState: AppState) {
    when {
      appState.firstRun -> navigateToCommand.value = SplashNavigator.ONBOARDING_SCREEN
      else -> navigateToCommand.value = SplashNavigator.MAIN_SCREEN
    }
  }

}
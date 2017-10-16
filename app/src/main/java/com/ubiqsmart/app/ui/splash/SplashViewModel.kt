package com.ubiqsmart.app.ui.splash

import android.app.Application
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.ubiqsmart.app.ui.base.BaseViewModel
import com.ubiqsmart.domain.interactors.splash.GetAppStateInteractor
import com.ubiqsmart.domain.models.AppState
import com.ubiqsmart.lifecycle.SingleLiveEvent
import com.ubiqsmart.rx.Schedulers
import java.util.concurrent.TimeUnit

class SplashViewModel(app: Application) : BaseViewModel(app) {

  private val getAppStateInteractor: GetAppStateInteractor by injector.with(this@SplashViewModel).instance()

  val onNavigateToCommand = SingleLiveEvent<Int>()
  val onErrorCommand = SingleLiveEvent<Throwable>()

  override fun provideOverridingModule() = Kodein.Module {
    import(SplashDI.Module)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    getAppState()
  }

  private fun getAppState() {
    val subscription = getAppStateInteractor.execute()
        .delay(1, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.ui())
        .subscribe({ appState: AppState?, throwable: Throwable? ->
          appState?.let { onNavigateTo(it) }
          throwable?.let { onError(it) }
        })

    disposables.add(subscription)
  }

  private fun onNavigateTo(appState: AppState) {
    when {
      appState.firstRun!! -> onNavigateToCommand.value = SplashNavigator.ONBOARDING_SCREEN
      else -> onNavigateToCommand.value = SplashNavigator.MAIN_SCREEN
    }
  }

  private fun onError(throwable: Throwable) {
    onErrorCommand.value = throwable
  }

}
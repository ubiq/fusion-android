package com.ubiqsmart.app.ui.onboarding

import android.app.Application
import com.github.salomonbrys.kodein.*
import com.ubiqsmart.app.ui.base.BaseViewModel
import com.ubiqsmart.di.ViewModelScope
import com.ubiqsmart.domain.interactors.splash.SaveAppStateInteractor
import com.ubiqsmart.domain.models.AppState
import com.ubiqsmart.domain.repositories.AppStateRepository
import com.ubiqsmart.lifecycle.SingleLiveEvent
import com.ubiqsmart.rx.Schedulers

class OnBoardingViewModel(app: Application) : BaseViewModel(app) {

  override fun provideOverridingModule() = Kodein.Module {
    bind<SaveAppStateInteractor>() with scopedSingleton(ViewModelScope) { SaveAppStateInteractor(AppStateRepository(instance())) }
  }

  private val saveAppStateInteractor: SaveAppStateInteractor by injector.with(this@OnBoardingViewModel).instance()

  val onNavigateToCommand = SingleLiveEvent<Int>()
  val onErrorCommand = SingleLiveEvent<Throwable>()

  fun saveMarkOnBoardingAsPassed() {
    val state = AppState(false)
    saveAppStateInteractor.execute(state)
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.ui())
        .subscribe({ appState: AppState?, throwable: Throwable? ->
          appState?.let { onNavigateToMainScreen() }
          throwable?.let { onError(throwable) }
        })
  }

  private fun onNavigateToMainScreen() {
    onNavigateToCommand.call()
  }

  private fun onError(throwable: Throwable) {
    onErrorCommand.value = throwable
  }

}
package com.ubiqsmart.app.ui.splash

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.scopedSingleton
import com.ubiqsmart.di.ViewModelScope
import com.ubiqsmart.domain.interactors.splash.GetAppStateInteractor
import com.ubiqsmart.domain.repositories.AppStateRepository

object SplashDI {

  val Module = Kodein.Module {

    bind<AppStateRepository>() with scopedSingleton(ViewModelScope) { AppStateRepository() }

    bind<GetAppStateInteractor>() with scopedSingleton(ViewModelScope) { GetAppStateInteractor(instance()) }

  }

}
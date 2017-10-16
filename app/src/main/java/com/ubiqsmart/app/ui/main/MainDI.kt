package com.ubiqsmart.app.ui.main

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.scopedSingleton
import com.ubiqsmart.di.ViewModelScope
import com.ubiqsmart.domain.interactors.exchanges.GetPriceExchangeInteractor
import com.ubiqsmart.domain.repositories.ExchangesRepository

object MainDI {

  val Module = Kodein.Module {

    bind<ExchangesRepository>() with scopedSingleton(ViewModelScope) { ExchangesRepository(instance()) }

    bind<GetPriceExchangeInteractor>() with scopedSingleton(ViewModelScope) { GetPriceExchangeInteractor(instance()) }

  }

}
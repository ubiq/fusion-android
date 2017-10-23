package com.ubiqsmart.app.ui.main

import android.app.Application
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.ubiqsmart.app.ui.base.BaseViewModel
import com.ubiqsmart.domain.interactors.exchanges.GetPriceExchangeInteractor
import com.ubiqsmart.domain.models.PriceExchange
import io.reactivex.rxkotlin.subscribeBy

class MainViewModel(app: Application) : BaseViewModel(app) {

  private val getPriceExchangeInteractor: GetPriceExchangeInteractor by injector.with(this@MainViewModel).instance()

  override fun provideOverridingModule() = Kodein.Module {
    import(MainDI.Module)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    fetchCurrentExchangeRate()
  }

  private fun fetchCurrentExchangeRate() {
    disposables.add(
        getPriceExchangeInteractor.execute()
            .subscribeBy(
                onSuccess = {
                  onExchangeRateFetched(it)
                },
                onError = {
                  onError(it)
                }
            )
    )
  }

  private fun onExchangeRateFetched(priceExchange: PriceExchange) {
  }

  private fun onError(throwable: Throwable) {
  }

}

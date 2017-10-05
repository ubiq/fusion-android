package com.ubiqsmart.app.ui.main

import android.app.Application
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.ubiqsmart.app.services.NotificationLauncher
import com.ubiqsmart.app.ui.base.BaseViewModel
import com.ubiqsmart.app.utils.Settings
import com.ubiqsmart.domain.interactors.exchanges.GetPriceExchangeInteractor
import com.ubiqsmart.domain.interactors.splash.GetAppStateInteractor
import io.reactivex.rxkotlin.subscribeBy

class MainViewModel(app: Application) : BaseViewModel(app) {

  private val getAppStateInteractor: GetAppStateInteractor by with(this@MainViewModel).instance()
  private val getPriceExchangeInteractor: GetPriceExchangeInteractor by with(this@MainViewModel).instance()

  private val notificationLauncher: NotificationLauncher by instance()

  override fun onViewCreated() {
    super.onViewCreated()
    fetchCurrentExchangeRate()
    Settings.initiate(getApplication())
    notificationLauncher.start()
  }

  override fun onDestroyView() {
    super.onDestroyView()
  }

  private fun fetchCurrentExchangeRate() {
    disposables.add(
        getPriceExchangeInteractor.execute()
            .subscribeBy(onSuccess = {
            }, onError = {

            })
    )

//      val currency = preferences.getString("maincurrency", "USD")
//      exchangeCalculator.updateExchangeRates(currency, NetworkUpdateListener {
//        runOnUiThread {
//          broadCastDataSetChanged()
//          (fragments[0] as PriceFragment).update()
//        }
//      })
  }

}

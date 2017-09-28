package com.ubiqsmart.domain.repositories

import com.ubiqsmart.datasource.api.CryptoCompareApi
import com.ubiqsmart.domain.models.CurrencyExchangeRate
import io.reactivex.Observable

class ExchangesRepository(val priceApi: CryptoCompareApi) {

  fun getPriceChart(currency: String = "USD"): Observable<CurrencyExchangeRate> {
    val exchangeRate = CurrencyExchangeRate(btc = 0.000436, usd = 1.63, eur = 1.37)
    return Observable.just(exchangeRate)
  }

}
package com.ubiqsmart.repository

import com.ubiqsmart.repository.api.CryptoCompareApi
import com.ubiqsmart.repository.data.CurrencyExchangeRate
import io.reactivex.Observable

class ExchangesRepository(val priceApi: CryptoCompareApi) {

    fun getPriceChart(currency: String = "USD"): Observable<CurrencyExchangeRate> {
        var exchangeRate = CurrencyExchangeRate(btc = 0.000436, usd = 1.63, eur = 1.37)
        return Observable.just(exchangeRate)
    }

}
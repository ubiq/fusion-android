package com.ubiqsmart.domain.repositories

import com.ubiqsmart.datasource.api.CryptoCompareApi
import com.ubiqsmart.domain.models.PriceExchange
import io.reactivex.Single

class ExchangesRepository(private val priceApi: CryptoCompareApi) {

  fun getPriceChart(): Single<PriceExchange> {
    return priceApi.getPriceChart().map { it.toDomain() }
  }

}
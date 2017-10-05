package com.ubiqsmart.domain.interactors.exchanges

import com.ubiqsmart.domain.models.PriceExchange
import com.ubiqsmart.domain.repositories.ExchangesRepository
import io.reactivex.Single

class GetPriceExchangeInteractor(private val repository: ExchangesRepository) {

  fun execute(): Single<PriceExchange> {
    return repository.getPriceChart()
  }

}